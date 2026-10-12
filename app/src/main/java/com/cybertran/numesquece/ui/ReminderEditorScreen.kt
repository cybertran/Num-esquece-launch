package com.cybertran.numesquece.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cybertran.numesquece.R
import com.cybertran.numesquece.data.ReminderEntity
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.data.RepeatMode
import com.cybertran.numesquece.data.intensityValue
import com.cybertran.numesquece.data.repeatModeValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderEditorScreen(
    reminder: ReminderEntity?,
    onBack: () -> Unit,
    onSave: (ReminderEntity) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val defaultDateTime = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0)
    var title by remember(reminder?.id) { mutableStateOf(reminder?.title.orEmpty()) }
    var notes by remember(reminder?.id) { mutableStateOf(reminder?.notes.orEmpty()) }
    var dueAt by remember(reminder?.id) {
        mutableLongStateOf(reminder?.dueAt ?: defaultDateTime.atZone(zone).toInstant().toEpochMilli())
    }
    var intensity by remember(reminder?.id) { mutableStateOf(reminder?.intensityValue() ?: ReminderIntensity.NORMAL) }
    var repeatMode by remember(reminder?.id) { mutableStateOf(reminder?.repeatModeValue() ?: RepeatMode.NONE) }
    var repeatIntervalDays by remember(reminder?.id) { mutableStateOf(reminder?.repeatIntervalDays ?: 30) }
    var windowEnabled by remember(reminder?.id) { mutableStateOf(reminder?.windowEndAt != null) }
    var windowHours by remember(reminder?.id) {
        mutableStateOf(
            reminder?.let { current ->
                current.windowEndAt
                    ?.let { end -> ((end - current.dueAt) / 3_600_000L).toInt().coerceAtLeast(1) }
                    ?: 2
            } ?: 2
        )
    }
    var confirmDelete by remember { mutableStateOf(false) }

    fun pickDateTime() {
        val current = Instant.ofEpochMilli(dueAt).atZone(zone).toLocalDateTime()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val chosenDate = LocalDate.of(year, month + 1, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        dueAt = LocalDateTime.of(chosenDate, LocalTime.of(hour, minute))
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()
                    },
                    current.hour,
                    current.minute,
                    true,
                ).show()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth,
        ).show()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (reminder == null) R.string.new_reminder else R.string.edit_reminder), fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } },
                actions = {
                    if (onDelete != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.reminder_title_prompt)) },
                minLines = 2,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.notes_optional)) },
                minLines = 2,
            )

            EditorSectionTitle(stringResource(R.string.reminder_when))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(onClick = { dueAt = quickDateTime(0, 18, 0) }, label = { Text(stringResource(R.string.quick_today_18)) })
                AssistChip(onClick = { dueAt = quickDateTime(1, 9, 0) }, label = { Text(stringResource(R.string.quick_tomorrow_9)) })
                AssistChip(onClick = { dueAt = System.currentTimeMillis() + 60 * 60_000L }, label = { Text(stringResource(R.string.quick_in_1h)) })
            }
            OutlinedButton(onClick = ::pickDateTime, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(Icons.Rounded.Schedule, null)
                Spacer(Modifier.width(8.dp))
                Text(formatDateTime(context, dueAt))
            }

            EditorSectionTitle(stringResource(R.string.how_to_remind))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderIntensity.entries.forEach { option ->
                    FilterChip(
                        selected = intensity == option,
                        onClick = { intensity = option },
                        label = {
                            Column(Modifier.padding(vertical = 2.dp)) {
                                Text(stringResource(option.labelRes), fontWeight = FontWeight.SemiBold)
                                Text(intensityExplanation(option), style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            EditorSectionTitle(stringResource(R.string.time_window))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.within_window), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.window_explanation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = windowEnabled, onCheckedChange = { windowEnabled = it })
            }
            if (windowEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf(1, 2, 4).forEach { hours ->
                        FilterChip(
                            selected = windowHours == hours,
                            onClick = { windowHours = hours },
                            label = { Text(stringResource(R.string.hours_short, hours)) },
                        )
                    }
                }
            }

            EditorSectionTitle(stringResource(R.string.repeat))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RepeatMode.entries.forEach { option ->
                    FilterChip(
                        selected = repeatMode == option,
                        onClick = { repeatMode = option },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }
            if (repeatMode == RepeatMode.AFTER_COMPLETION) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.repeat_prefix) + " ")
                    listOf(7, 15, 30, 60).forEach { days ->
                        AssistChip(
                            onClick = { repeatIntervalDays = days },
                            label = {
                                val dayText = pluralStringResource(R.plurals.days_short, days, days)
                                Text(if (repeatIntervalDays == days) stringResource(R.string.selected_value, dayText) else dayText)
                            },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    val base = reminder ?: ReminderEntity(title = title.trim(), dueAt = dueAt)
                    onSave(
                        base.copy(
                            title = title.trim(),
                            notes = notes.trim(),
                            dueAt = dueAt,
                            windowEndAt = if (windowEnabled) dueAt + windowHours * 3_600_000L else null,
                            intensity = intensity.name,
                            repeatMode = repeatMode.name,
                            repeatIntervalDays = repeatIntervalDays,
                        )
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(stringResource(R.string.save_reminder)) }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_reminder_title)) },
            text = { Text(stringResource(R.string.delete_reminder_body)) },
            confirmButton = { TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun intensityExplanation(option: ReminderIntensity): String = stringResource(
    when (option) {
        ReminderIntensity.NORMAL -> R.string.intensity_normal_explanation
        ReminderIntensity.INSISTENT -> R.string.intensity_insistent_explanation
        ReminderIntensity.DONT_LET_ME_FORGET -> R.string.intensity_dont_forget_explanation
    }
)

@Composable
private fun EditorSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
    )
}

private fun quickDateTime(daysFromToday: Long, hour: Int, minute: Int): Long =
    LocalDate.now()
        .plusDays(daysFromToday)
        .atTime(hour, minute)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
