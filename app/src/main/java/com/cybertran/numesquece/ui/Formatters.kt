package com.cybertran.numesquece.ui

import android.content.Context
import com.cybertran.numesquece.R
import com.cybertran.numesquece.data.ReminderEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun reminderStateText(context: Context, reminder: ReminderEntity, now: Long): String {
    val windowEnd = reminder.deadlineAt
    val prefix = when {
        reminder.dueAt > now -> formatWhen(context, reminder.dueAt)
        windowEnd != null && now <= windowEnd -> context.getString(R.string.until_time, formatTime(context, windowEnd))
        windowEnd != null && now > windowEnd -> context.getString(R.string.window_closed)
        else -> context.getString(R.string.since_time, formatWhen(context, reminder.dueAt))
    }
    val snooze = if (reminder.snoozeCount > 0) {
        context.resources.getQuantityString(
            R.plurals.snoozed_count,
            reminder.snoozeCount,
            reminder.snoozeCount,
        )
    } else {
        ""
    }
    return prefix + snooze
}

internal fun formatWhen(context: Context, timestamp: Long): String {
    val zone = ZoneId.systemDefault()
    val value = Instant.ofEpochMilli(timestamp).atZone(zone)
    val today = LocalDate.now(zone)
    val locale = context.resources.configuration.locales[0]
    val dateText = when (value.toLocalDate()) {
        today -> context.getString(R.string.today)
        today.plusDays(1) -> context.getString(R.string.tomorrow)
        else -> value.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))
    }
    val timeText = value.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    return context.getString(R.string.relative_date_time, dateText, timeText)
}

internal fun formatDateTime(context: Context, timestamp: Long): String {
    val value = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    val locale = context.resources.configuration.locales[0]
    return value.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale))
}

private fun formatTime(context: Context, timestamp: Long): String {
    val value = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    val locale = context.resources.configuration.locales[0]
    return value.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
}
