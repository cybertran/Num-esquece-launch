package com.cybertran.numesquece.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cybertran.numesquece.R
import com.cybertran.numesquece.data.ReminderEntity
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.data.ReminderStatus
import com.cybertran.numesquece.data.intensityValue
import com.cybertran.numesquece.data.statusValue
import kotlinx.coroutines.delay

@Composable
internal fun NowScreen(
    reminders: List<ReminderEntity>,
    padding: PaddingValues,
    onComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onOpen: (ReminderEntity) -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = System.currentTimeMillis()
        }
    }

    val active = reminders.filter { it.statusValue() == ReminderStatus.ACTIVE }.sortedBy { it.dueAt }
    val pending = active.filter { it.dueAt <= now }
    val upcoming = active.filter { it.dueAt > now }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (pending.isEmpty()) {
                CalmStateCard(next = upcoming.firstOrNull())
            } else {
                Text(
                    pluralStringResource(R.plurals.pending_attention, pending.size, pending.size),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (pending.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.section_pending)) }
            items(pending, key = { it.id }) { reminder ->
                ReminderActionCard(reminder, now, onComplete, onSnooze, onOpen)
            }
        }

        if (upcoming.isNotEmpty()) {
            item { SectionLabel(stringResource(if (pending.isEmpty()) R.string.section_next else R.string.section_later)) }
            items(upcoming.take(6), key = { it.id }) { reminder ->
                ReminderCompactCard(reminder, now, onOpen)
            }
        }

        if (active.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.empty_create_reminder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun CalmStateCard(next: ReminderEntity?) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(22.dp)) {
            Icon(Icons.Rounded.TaskAlt, null, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.all_caught_up), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (next == null) stringResource(R.string.no_pending_reminders) else stringResource(R.string.next_scheduled_for, formatWhen(context, next.dueAt)),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
    )
}

@Composable
private fun ReminderActionCard(
    reminder: ReminderEntity,
    now: Long,
    onComplete: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onOpen: (ReminderEntity) -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(reminder) },
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.deadlineAt != null && now > reminder.deadlineAt) {
                MaterialTheme.colorScheme.errorContainer
            } else MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(reminder.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        reminderStateText(context, reminder, now),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                IntensityLabel(reminder.intensityValue())
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onComplete(reminder.id) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Check, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.done))
                }
                OutlinedButton(onClick = { onSnooze(reminder.id) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.snooze_10))
                }
            }
        }
    }
}

@Composable
private fun ReminderCompactCard(reminder: ReminderEntity, now: Long, onOpen: (ReminderEntity) -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(reminder) }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    reminderStateText(context, reminder, now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IntensityLabel(reminder.intensityValue())
        }
    }
}

@Composable
private fun IntensityLabel(intensity: ReminderIntensity) {
    val color = when (intensity) {
        ReminderIntensity.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
        ReminderIntensity.INSISTENT -> MaterialTheme.colorScheme.secondary
        ReminderIntensity.DONT_LET_ME_FORGET -> MaterialTheme.colorScheme.primary
    }
    Text(
        text = stringResource(intensity.labelRes),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 12.dp),
    )
}

@Composable
internal fun ReminderListScreen(
    reminders: List<ReminderEntity>,
    padding: PaddingValues,
    onOpen: (ReminderEntity) -> Unit,
) {
    val active = reminders.filter { it.statusValue() == ReminderStatus.ACTIVE }.sortedBy { it.dueAt }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = System.currentTimeMillis()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(18.dp, 8.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (active.isEmpty()) item { EmptyListMessage(stringResource(R.string.no_active_reminders)) }
        else items(active, key = { it.id }) { reminder -> ReminderCompactCard(reminder, now, onOpen) }
    }
}

@Composable
internal fun HistoryScreen(reminders: List<ReminderEntity>, padding: PaddingValues, onClearHistory: () -> Unit) {
    val context = LocalContext.current
    val completed = reminders
        .filter { it.statusValue() == ReminderStatus.COMPLETED }
        .sortedByDescending { it.completedAt ?: 0L }
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(18.dp, 8.dp, 18.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (completed.isEmpty()) {
            item { EmptyListMessage(stringResource(R.string.history_empty)) }
        } else {
            item {
                OutlinedButton(
                    onClick = { confirmClear = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.clear_history))
                }
            }
            items(completed, key = { it.id }) { reminder ->
                Card(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(reminder.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(stringResource(R.string.completed_at, reminder.completedAt?.let { formatWhen(context, it) }.orEmpty())) },
                        leadingContent = {
                            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        },
                    )
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.clear_history_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        confirmClear = false
                    }
                ) { Text(stringResource(R.string.clear_history)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptyListMessage(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Rounded.NotificationsActive,
            null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
    }
}
