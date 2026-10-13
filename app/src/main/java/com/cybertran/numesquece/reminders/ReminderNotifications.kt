package com.cybertran.numesquece.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cybertran.numesquece.MainActivity
import com.cybertran.numesquece.NotificationSoundMode
import com.cybertran.numesquece.R
import com.cybertran.numesquece.SettingsStore
import com.cybertran.numesquece.data.ReminderEntity
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.data.intensityValue

object ReminderNotifications {
    private const val GROUP_ID = "reminders_group"
    private const val CHANNEL_NORMAL_APP = "reminders_normal_num_v1"
    private const val CHANNEL_INSISTENT_APP = "reminders_insistent_num_v1"
    private const val CHANNEL_DONT_FORGET_APP = "reminders_dont_forget_num_v1"
    private const val CHANNEL_SYSTEM = "reminders_system_v1"
    private const val CHANNEL_SILENT = "reminders_silent_v1"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_ID, context.getString(R.string.channel_group_reminders))
        )

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channels = listOf(
            appSoundChannel(
                context = context,
                id = CHANNEL_NORMAL_APP,
                name = context.getString(R.string.channel_normal),
                soundRes = R.raw.num_normal,
                audioAttributes = audioAttributes,
            ),
            appSoundChannel(
                context = context,
                id = CHANNEL_INSISTENT_APP,
                name = context.getString(R.string.channel_insistent),
                soundRes = R.raw.num_insistente,
                audioAttributes = audioAttributes,
            ),
            appSoundChannel(
                context = context,
                id = CHANNEL_DONT_FORGET_APP,
                name = context.getString(R.string.channel_dont_forget),
                soundRes = R.raw.num_nao_esquece,
                audioAttributes = audioAttributes,
            ),
            NotificationChannel(
                CHANNEL_SYSTEM,
                context.getString(R.string.sound_system),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                group = GROUP_ID
                description = context.getString(R.string.channel_system_description)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            },
            NotificationChannel(
                CHANNEL_SILENT,
                context.getString(R.string.sound_silent),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                group = GROUP_ID
                description = context.getString(R.string.channel_silent_description)
                enableVibration(false)
                setSound(null, null)
            },
        )

        manager.createNotificationChannels(channels)
    }

    fun show(context: Context, reminder: ReminderEntity): Boolean {
        val openApp = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val done = actionIntent(context, reminder.id, ReminderReceiver.ACTION_DONE, 1)
        val snooze = actionIntent(context, reminder.id, ReminderReceiver.ACTION_SNOOZE_10, 2)
        val dismissed = actionIntent(context, reminder.id, ReminderReceiver.ACTION_DISMISSED, 3)

        val supportingText = context.getString(
            when (reminder.intensityValue()) {
                ReminderIntensity.NORMAL -> R.string.notification_pending
                ReminderIntensity.INSISTENT -> R.string.notification_insistent
                ReminderIntensity.DONT_LET_ME_FORGET -> R.string.notification_dont_forget
            }
        )

        val channelId = channelFor(context, reminder)
        val showOnLockScreen = SettingsStore(context).showContentOnLockScreen
        val publicVersion = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_hidden_content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(supportingText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(supportingText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(
                if (showOnLockScreen) NotificationCompat.VISIBILITY_PUBLIC
                else NotificationCompat.VISIBILITY_PRIVATE
            )
            .setPublicVersion(publicVersion)
            .setAutoCancel(false)
            .setContentIntent(openApp)
            .setDeleteIntent(dismissed)
            .addAction(0, context.getString(R.string.done), done)
            .addAction(0, context.getString(R.string.snooze_10), snooze)
            .build()

        val canPostNotifications =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        if (!canPostNotifications) return false

        return runCatching {
            NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
        }.isSuccess
    }

    fun preview(
        context: Context,
        intensity: ReminderIntensity,
        mode: NotificationSoundMode,
    ) {
        when (mode) {
            NotificationSoundMode.NUM_ESQUECE -> {
                val soundRes = soundFor(intensity)
                MediaPlayer.create(context, soundRes)?.apply {
                    setOnCompletionListener { player -> player.release() }
                    start()
                }
            }

            NotificationSoundMode.SYSTEM -> {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, uri)?.play()
            }

            NotificationSoundMode.SILENT -> Unit
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(reminderId.hashCode())
    }

    private fun appSoundChannel(
        context: Context,
        id: String,
        name: String,
        soundRes: Int,
        audioAttributes: AudioAttributes,
    ): NotificationChannel =
        NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
            group = GROUP_ID
            description = context.getString(R.string.channel_app_sound_description)
            enableVibration(true)
            setSound(resourceUri(context, soundRes), audioAttributes)
        }

    private fun channelFor(context: Context, reminder: ReminderEntity): String {
        return when (SettingsStore(context).notificationSoundMode) {
            NotificationSoundMode.SYSTEM -> CHANNEL_SYSTEM
            NotificationSoundMode.SILENT -> CHANNEL_SILENT
            NotificationSoundMode.NUM_ESQUECE -> when (reminder.intensityValue()) {
                ReminderIntensity.NORMAL -> CHANNEL_NORMAL_APP
                ReminderIntensity.INSISTENT -> CHANNEL_INSISTENT_APP
                ReminderIntensity.DONT_LET_ME_FORGET -> CHANNEL_DONT_FORGET_APP
            }
        }
    }

    private fun soundFor(intensity: ReminderIntensity): Int = when (intensity) {
        ReminderIntensity.NORMAL -> R.raw.num_normal
        ReminderIntensity.INSISTENT -> R.raw.num_insistente
        ReminderIntensity.DONT_LET_ME_FORGET -> R.raw.num_nao_esquece
    }

    private fun resourceUri(context: Context, soundRes: Int): Uri {
        val entryName = context.resources.getResourceEntryName(soundRes)
        return Uri.parse("android.resource://" + context.packageName + "/raw/" + entryName)
    }

    private fun actionIntent(
        context: Context,
        reminderId: Long,
        action: String,
        salt: Int,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        return PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() * 10 + salt,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
