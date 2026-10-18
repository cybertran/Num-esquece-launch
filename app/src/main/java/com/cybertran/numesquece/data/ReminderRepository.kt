package com.cybertran.numesquece.data

import com.cybertran.numesquece.reminders.ReminderScheduler
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun get(id: Long): ReminderEntity? = dao.byId(id)

    suspend fun save(reminder: ReminderEntity): Long {
        val zone = ZoneId.systemDefault()
        val repeat = reminder.repeatModeValue()
        val dueEpochDay = Instant.ofEpochMilli(reminder.dueAt).atZone(zone).toLocalDate().toEpochDay()
        val repeatUntil = if (repeat == RepeatMode.NONE) {
            null
        } else {
            reminder.repeatUntilEpochDay?.coerceAtLeast(dueEpochDay)
        }
        val anchorDay = if (repeat == RepeatMode.MONTHLY) {
            reminder.monthlyAnchorDay
                ?: Instant.ofEpochMilli(reminder.dueAt).atZone(zone).dayOfMonth
        } else {
            null
        }
        val deadlineOffset = reminder.deadlineAt?.let { deadline ->
            reminder.deadlineOffsetMinutes
                ?: ((deadline - reminder.dueAt) / 60_000L)
                    .takeIf { it > 0 }
                    ?.coerceAtMost(Int.MAX_VALUE.toLong())
                    ?.toInt()
        }
        val prepared = reminder.copy(
            deadlineOffsetMinutes = deadlineOffset,
            monthlyAnchorDay = anchorDay,
            repeatUntilEpochDay = repeatUntil,
        )

        val id = if (prepared.id == 0L) {
            dao.insert(prepared)
        } else {
            dao.update(prepared)
            prepared.id
        }
        dao.byId(id)?.let(scheduler::schedule)
        return id
    }

    suspend fun complete(id: Long) {
        val reminder = dao.byId(id) ?: return
        if (reminder.statusValue() != ReminderStatus.ACTIVE) return

        val completedAt = System.currentTimeMillis()
        dao.update(
            reminder.copy(
                status = ReminderStatus.COMPLETED.name,
                completedAt = completedAt,
            )
        )
        scheduler.cancel(reminder.id)

        val nextDueAt = ReminderLogic.nextOccurrence(reminder, completedAt)
        if (nextDueAt != null) {
            val deadlineOffset = reminder.deadlineOffsetMinutes
            val next = reminder.copy(
                id = 0,
                dueAt = nextDueAt,
                deadlineAt = deadlineOffset?.let { nextDueAt + it * 60_000L },
                status = ReminderStatus.ACTIVE.name,
                snoozeCount = 0,
                createdAt = completedAt,
                completedAt = null,
            )
            val nextId = dao.insert(next)
            dao.byId(nextId)?.let(scheduler::schedule)
        }
    }

    suspend fun snooze(id: Long, minutes: Int) {
        val reminder = dao.byId(id) ?: return
        if (reminder.statusValue() != ReminderStatus.ACTIVE) return

        val updated = ReminderLogic.snoozed(
            reminder = reminder,
            now = System.currentTimeMillis(),
            minutes = minutes,
        )
        dao.update(updated)
        scheduler.schedule(updated)
    }

    suspend fun delete(id: Long) {
        val reminder = dao.byId(id) ?: return
        scheduler.cancel(id)
        dao.delete(reminder.id)
    }

    suspend fun clearHistory() {
        dao.clearCompleted()
    }

    suspend fun clearAll() {
        dao.active().forEach { scheduler.cancel(it.id) }
        dao.clearAll()
    }

    suspend fun rescheduleActive() {
        dao.active().forEach(scheduler::schedule)
    }
}
