package com.cybertran.numesquece.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.min

internal object ReminderLogic {
    fun snoozed(
        reminder: ReminderEntity,
        now: Long,
        minutes: Int,
    ): ReminderEntity {
        require(minutes > 0)
        return reminder.copy(
            dueAt = now + minutes * 60_000L,
            snoozeCount = reminder.snoozeCount + 1,
        )
    }

    fun nextOccurrence(
        reminder: ReminderEntity,
        completedAt: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long? {
        val repeat = reminder.repeatModeValue()
        if (repeat == RepeatMode.NONE) return null

        if (repeat == RepeatMode.AFTER_COMPLETION) {
            val days = reminder.repeatIntervalDays.coerceAtLeast(1).toLong()
            return Instant.ofEpochMilli(completedAt)
                .atZone(zone)
                .plusDays(days)
                .toInstant()
                .toEpochMilli()
        }

        val start = Instant.ofEpochMilli(reminder.dueAt).atZone(zone)
        var next = start

        if (repeat == RepeatMode.MONTHLY) {
            val anchorDay = reminder.monthlyAnchorDay ?: start.dayOfMonth
            var cursorDate = start.toLocalDate()
            val time = start.toLocalTime()

            do {
                val monthStart = cursorDate.withDayOfMonth(1).plusMonths(1)
                val targetDay = min(anchorDay, monthStart.lengthOfMonth())
                cursorDate = monthStart.withDayOfMonth(targetDay)
                next = LocalDateTime.of(cursorDate, time).atZone(zone)
            } while (next.toInstant().toEpochMilli() <= completedAt)

            return next.toInstant().toEpochMilli()
        }

        do {
            next = when (repeat) {
                RepeatMode.DAILY -> next.plusDays(1)
                RepeatMode.WEEKLY -> next.plusWeeks(1)
                else -> next
            }
        } while (next.toInstant().toEpochMilli() <= completedAt)

        return next.toInstant().toEpochMilli()
    }
}
