package com.cybertran.numesquece.ui

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.cybertran.numesquece.BuildConfig
import com.cybertran.numesquece.R
import com.cybertran.numesquece.NotificationSoundMode
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.reminders.ReminderNotifications
import com.cybertran.numesquece.sync.GoogleSyncState
import com.cybertran.numesquece.sync.SyncPhase

@Composable
internal fun SettingsScreen(
    padding: PaddingValues,
    dynamicColor: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    onTestReminder: () -> Unit,
    notificationSoundMode: NotificationSoundMode,
    onNotificationSoundModeChanged: (NotificationSoundMode) -> Unit,
    syncState: GoogleSyncState,
    onConnectGoogle: () -> Unit,
    onSyncGoogle: () -> Unit,
    onDisconnectGoogle: () -> Unit,
) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val exactAlarmEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    val syncBusy = syncState.phase == SyncPhase.CONNECTING || syncState.phase == SyncPhase.SYNCING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        SettingsCard(stringResource(R.string.reliability)) {
            SettingsStatusRow(stringResource(R.string.notifications), notificationsEnabled)
            SettingsStatusRow(stringResource(R.string.exact_alarms), exactAlarmEnabled)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onTestReminder, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.create_test_reminder))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmEnabled) {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + context.packageName),
                        )
                        runCatching { context.startActivity(intent) }
                    }
                ) { Text(stringResource(R.string.allow_exact_alarms)) }
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.reminder_sound)) {
            Text(
                stringResource(R.string.reminder_sound_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            NotificationSoundMode.entries.forEach { mode ->
                FilterChip(
                    selected = notificationSoundMode == mode,
                    onClick = { onNotificationSoundModeChanged(mode) },
                    label = { Text(stringResource(mode.labelRes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))
            when (notificationSoundMode) {
                NotificationSoundMode.NUM_ESQUECE -> {
                    Text(
                        stringResource(R.string.listen_custom_sounds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.NORMAL,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        }
                    ) { Text(stringResource(R.string.intensity_normal)) }
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.INSISTENT,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        }
                    ) { Text(stringResource(R.string.intensity_insistent)) }
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.DONT_LET_ME_FORGET,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        }
                    ) { Text(stringResource(R.string.intensity_dont_forget)) }
                }

                NotificationSoundMode.SYSTEM -> {
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.NORMAL,
                                NotificationSoundMode.SYSTEM,
                            )
                        }
                    ) { Text(stringResource(R.string.listen_default_sound)) }
                }

                NotificationSoundMode.SILENT -> {
                    Text(
                        stringResource(R.string.silent_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                stringResource(R.string.android_channel_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        if (BuildConfig.GOOGLE_SYNC_AVAILABLE) {
            Spacer(Modifier.height(12.dp))
            SettingsCard(stringResource(R.string.sync_title)) {
            Text(
                if (syncState.connected) stringResource(R.string.google_account_connected) else stringResource(R.string.only_this_device),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.sync_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            syncState.lastSyncAt?.let {
                Text(
                    stringResource(R.string.last_sync_at, formatDateTime(context, it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            syncState.message?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (syncState.phase == SyncPhase.ERROR) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            if (!syncState.connected) {
                OutlinedButton(
                    onClick = onConnectGoogle,
                    enabled = !syncBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (syncBusy) stringResource(R.string.connecting) else stringResource(R.string.connect_google_account))
                }
            } else {
                OutlinedButton(
                    onClick = onSyncGoogle,
                    enabled = !syncBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (syncBusy) stringResource(R.string.syncing) else stringResource(R.string.sync_now))
                }
                TextButton(
                    onClick = onConnectGoogle,
                    enabled = !syncBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.choose_another_google_account)) }
                TextButton(
                    onClick = onDisconnectGoogle,
                    enabled = !syncBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.disconnect_google_account)) }
            }
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.appearance)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.system_colors), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.material_you_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChanged)
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.privacy)) {
            Text(
                stringResource(
                    if (BuildConfig.GOOGLE_SYNC_AVAILABLE) R.string.privacy_google else R.string.privacy_local
                )
            )
        }

        if (BuildConfig.DONATION_URL.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SettingsCard(stringResource(R.string.support_num_esquece)) {
                Text(
                    stringResource(R.string.support_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DONATION_URL))
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.make_donation))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.about)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.open_source_description),
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.SOURCE_CODE_URL))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.view_source_code))
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsStatusRow(label: String, enabled: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Text(
            if (enabled) stringResource(R.string.status_active) else stringResource(R.string.status_attention),
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
