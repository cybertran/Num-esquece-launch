package com.cybertran.numesquece.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cybertran.numesquece.NumEsqueceApplication
import com.cybertran.numesquece.data.ReminderStatus
import com.cybertran.numesquece.data.intensityValue
import com.cybertran.numesquece.data.statusValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as NumEsqueceApplication
                when (intent.action) {
                    ACTION_FIRE -> {
                        val reminder = app.repository.get(reminderId)
                        if (reminder?.statusValue() == ReminderStatus.ACTIVE) {
                            ReminderNotifications.show(context, reminder)
                        }
                    }

                    ACTION_DONE -> {
                        ReminderNotifications.cancel(context, reminderId)
                        app.repository.complete(reminderId)
                    }

                    ACTION_SNOOZE_10 -> {
                        ReminderNotifications.cancel(context, reminderId)
                        app.repository.snooze(reminderId, 10)
                    }

                    ACTION_DISMISSED -> {
                        val reminder = app.repository.get(reminderId)
                        val minutes = reminder?.intensityValue()?.repeatMinutesAfterDismiss
                        if (reminder?.statusValue() == ReminderStatus.ACTIVE && minutes != null) {
                            app.scheduler.scheduleAt(
                                reminderId,
                                System.currentTimeMillis() + minutes * 60_000L,
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_FIRE = "com.cybertran.numesquece.FIRE"
        const val ACTION_DONE = "com.cybertran.numesquece.DONE"
        const val ACTION_SNOOZE_10 = "com.cybertran.numesquece.SNOOZE_10"
        const val ACTION_DISMISSED = "com.cybertran.numesquece.DISMISSED"
    }
}
