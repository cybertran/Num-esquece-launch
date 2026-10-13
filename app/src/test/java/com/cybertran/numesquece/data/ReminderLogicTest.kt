package com.cybertran.numesquece.data

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderLogicTest {
    private val utc = ZoneId.of("UTC")

    @Test
    fun monthlyRecurrencePreservesOriginalDayAcrossShortMonth() {
        val january31 = at(utc, 2027, 1, 31, 9, 0)
        val reminder = ReminderEntity(
            title = "Monthly",
            dueAt = january31,
            repeatMode = RepeatMode.MONTHLY.name,
            monthlyAnchorDay = 31,
        )

        val february = ReminderLogic.nextOccurrence(reminder, january31 + 60_000L, utc)
        assertEquals(at(utc, 2027, 2, 28, 9, 0), february)

        val march = ReminderLogic.nextOccurrence(
            reminder.copy(dueAt = february!!),
            february + 60_000L,
            utc,
        )
        assertEquals(at(utc, 2027, 3, 31, 9, 0), march)
    }

    @Test
    fun monthlyRecurrenceUsesLastDayWhenAnchorDoesNotExist() {
        val march31 = at(utc, 2026, 3, 31, 14, 30)
        val reminder = ReminderEntity(
            title = "Monthly",
            dueAt = march31,
            repeatMode = RepeatMode.MONTHLY.name,
            monthlyAnchorDay = 31,
        )

        assertEquals(
            at(utc, 2026, 4, 30, 14, 30),
            ReminderLogic.nextOccurrence(reminder, march31 + 1, utc),
        )
    }

    @Test
    fun dailyRecurrenceKeepsLocalClockAcrossDstChange() {
        val zone = ZoneId.of("America/New_York")
        val beforeDst = at(zone, 2026, 3, 7, 9, 0)
        val reminder = ReminderEntity(
            title = "Daily",
            dueAt = beforeDst,
            repeatMode = RepeatMode.DAILY.name,
        )

        assertEquals(
            at(zone, 2026, 3, 8, 9, 0),
            ReminderLogic.nextOccurrence(reminder, beforeDst + 60_000L, zone),
        )
    }

    @Test
    fun afterCompletionUsesCompletionTimeAsAnchor() {
        val completed = at(utc, 2026, 10, 12, 18, 45)
        val reminder = ReminderEntity(
            title = "After completion",
            dueAt = completed - 60_000L,
            repeatMode = RepeatMode.AFTER_COMPLETION.name,
            repeatIntervalDays = 15,
        )

        assertEquals(
            at(utc, 2026, 10, 27, 18, 45),
            ReminderLogic.nextOccurrence(reminder, completed, utc),
        )
    }

    @Test
    fun snoozeMovesOnlyNextAlertAndKeepsDeadline() {
        val deadline = at(utc, 2026, 10, 12, 20, 0)
        val reminder = ReminderEntity(
            title = "Deadline",
            dueAt = at(utc, 2026, 10, 12, 18, 0),
            deadlineAt = deadline,
            deadlineOffsetMinutes = 120,
            snoozeCount = 2,
        )
        val now = at(utc, 2026, 10, 12, 18, 5)

        val snoozed = ReminderLogic.snoozed(reminder, now, 10)

        assertEquals(now + 10 * 60_000L, snoozed.dueAt)
        assertEquals(deadline, snoozed.deadlineAt)
        assertEquals(3, snoozed.snoozeCount)
    }

    @Test
    fun nonRecurringReminderHasNoNextOccurrence() {
        val reminder = ReminderEntity(
            title = "Once",
            dueAt = at(utc, 2026, 10, 12, 18, 0),
            repeatMode = RepeatMode.NONE.name,
        )
        assertNull(ReminderLogic.nextOccurrence(reminder, reminder.dueAt + 1, utc))
    }

    private fun at(
        zone: ZoneId,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)
        .toInstant()
        .toEpochMilli()
}
