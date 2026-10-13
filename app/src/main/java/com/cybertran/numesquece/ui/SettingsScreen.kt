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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.cybertran.numesquece.BuildConfig
import com.cybertran.numesquece.NotificationSoundMode
import com.cybertran.numesquece.R
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.reminders.ReminderNotifications

@Composable
internal fun SettingsScreen(
    padding: PaddingValues,
    dynamicColor: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    onTestReminder: () -> Unit,
    notificationSoundMode: NotificationSoundMode,
    onNotificationSoundModeChanged: (NotificationSoundMode) -> Unit,
    showContentOnLockScreen: Boolean,
    onShowContentOnLockScreenChanged: (Boolean) -> Unit,
    onDeleteAllData: () -> Unit,
) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val exactAlarmEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    var confirmDeleteAll by remember { mutableStateOf(false) }

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
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.show_lockscreen_content),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = showContentOnLockScreen,
                    onCheckedChange = onShowContentOnLockScreenChanged,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.reminder_sound)) {
            NotificationSoundMode.entries.forEach { mode ->
                FilterChip(
                    selected = notificationSoundMode == mode,
                    onClick = { onNotificationSoundModeChanged(mode) },
                    label = { Text(stringResource(mode.labelRes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            when (notificationSoundMode) {
                NotificationSoundMode.NUM_ESQUECE -> {
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.NORMAL,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.intensity_normal))
                    }
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.INSISTENT,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.intensity_insistent))
                    }
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.DONT_LET_ME_FORGET,
                                NotificationSoundMode.NUM_ESQUECE,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.intensity_dont_forget))
                    }
                }

                NotificationSoundMode.SYSTEM -> {
                    TextButton(
                        onClick = {
                            ReminderNotifications.preview(
                                context,
                                ReminderIntensity.NORMAL,
                                NotificationSoundMode.SYSTEM,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.listen_default_sound))
                    }
                }

                NotificationSoundMode.SILENT -> Unit
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.appearance)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.system_colors),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChanged)
            }
        }

        Spacer(Modifier.height(12.dp))
        SettingsCard(stringResource(R.string.data)) {
            OutlinedButton(
                onClick = { confirmDeleteAll = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.delete_all_data))
            }
        }

        if (BuildConfig.DONATION_URL.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            SettingsCard(stringResource(R.string.support_num_esquece)) {
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

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.delete_all_data_title)) },
            text = { Text(stringResource(R.string.delete_all_data_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteAll = false
                        onDeleteAllData()
                    }
                ) { Text(stringResource(R.string.delete_all_data)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
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
