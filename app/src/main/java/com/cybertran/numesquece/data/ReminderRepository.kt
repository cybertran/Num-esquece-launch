package com.cybertran.numesquece.data

import com.cybertran.numesquece.reminders.ReminderScheduler
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
    private val onChanged: () -> Unit = {},
) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun get(id: Long): ReminderEntity? = dao.byId(id)

    suspend fun save(reminder: ReminderEntity): Long {
        val now = System.currentTimeMillis()
        val prepared = reminder.copy(
            syncId = reminder.syncId.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
        )
        val id = if (prepared.id == 0L) {
            dao.insert(prepared)
        } else {
            dao.update(prepared)
            prepared.id
        }
        dao.byId(id)?.let(scheduler::schedule)
        onChanged()
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
                updatedAt = completedAt,
            )
        )
        scheduler.cancel(reminder.id)

        val nextDueAt = nextOccurrence(reminder, completedAt)
        if (nextDueAt != null) {
            val windowDuration = reminder.windowEndAt?.minus(reminder.dueAt)
            val next = reminder.copy(
                id = 0,
                dueAt = nextDueAt,
                windowEndAt = windowDuration?.let { nextDueAt + it },
                status = ReminderStatus.ACTIVE.name,
                snoozeCount = 0,
                createdAt = completedAt,
                completedAt = null,
                syncId = UUID.randomUUID().toString(),
                updatedAt = completedAt,
            )
            val nextId = dao.insert(next)
            dao.byId(nextId)?.let(scheduler::schedule)
        }
        onChanged()
    }

    suspend fun snooze(id: Long, minutes: Int) {
        val reminder = dao.byId(id) ?: return
        if (reminder.statusValue() != ReminderStatus.ACTIVE) return

        val oldDueAt = reminder.dueAt
        val now = System.currentTimeMillis()
        val newDueAt = now + minutes * 60_000L
        val shiftedWindowEnd = reminder.windowEndAt?.let { it + (newDueAt - oldDueAt) }
        val updated = reminder.copy(
            dueAt = newDueAt,
            windowEndAt = shiftedWindowEnd,
            snoozeCount = reminder.snoozeCount + 1,
            updatedAt = now,
        )
        dao.update(updated)
        scheduler.schedule(updated)
        onChanged()
    }

    suspend fun delete(id: Long) {
        val reminder = dao.byId(id) ?: return
        scheduler.cancel(id)
        dao.update(
            reminder.copy(
                status = ReminderStatus.DELETED.name,
                updatedAt = System.currentTimeMillis(),
            )
        )
        onChanged()
    }

    suspend fun rescheduleActive() {
        ensureSyncMetadata()
        dao.active().forEach(scheduler::schedule)
    }

    suspend fun snapshotForSync(): List<ReminderEntity> {
        ensureSyncMetadata()
        return dao.allOnce()
    }

    suspend fun mergeFromSync(remote: List<ReminderEntity>): Int {
        ensureSyncMetadata()
        var changed = 0

        remote
            .asSequence()
            .filter { it.syncId.isNotBlank() }
            .forEach { incoming ->
                val local = dao.bySyncId(incoming.syncId)
                if (local == null) {
                    val id = dao.insert(incoming.copy(id = 0))
                    dao.byId(id)?.let(::applyScheduleState)
                    changed += 1
                } else if (incoming.updatedAt > local.updatedAt) {
                    val merged = incoming.copy(id = local.id)
                    dao.update(merged)
                    applyScheduleState(merged)
                    changed += 1
                }
            }

        return changed
    }

    private suspend fun ensureSyncMetadata() {
        val now = System.currentTimeMillis()
        dao.allOnce().forEach { reminder ->
            if (reminder.syncId.isBlank() || reminder.updatedAt <= 0L) {
                dao.update(
                    reminder.copy(
                        syncId = reminder.syncId.ifBlank { UUID.randomUUID().toString() },
                        updatedAt = if (reminder.updatedAt > 0L) reminder.updatedAt else now,
                    )
                )
            }
        }
    }

    private fun applyScheduleState(reminder: ReminderEntity) {
        if (reminder.statusValue() == ReminderStatus.ACTIVE) {
            scheduler.schedule(reminder)
        } else {
            scheduler.cancel(reminder.id)
        }
    }

    private fun nextOccurrence(reminder: ReminderEntity, completedAt: Long): Long? {
        val repeat = reminder.repeatModeValue()
        if (repeat == RepeatMode.NONE) return null

        val zone = ZoneId.systemDefault()
        if (repeat == RepeatMode.AFTER_COMPLETION) {
            val days = reminder.repeatIntervalDays.coerceAtLeast(1).toLong()
            return Instant.ofEpochMilli(completedAt)
                .atZone(zone)
                .plusDays(days)
                .toInstant()
                .toEpochMilli()
        }

        var next = Instant.ofEpochMilli(reminder.dueAt).atZone(zone)
        do {
            next = when (repeat) {
                RepeatMode.DAILY -> next.plusDays(1)
                RepeatMode.WEEKLY -> next.plusWeeks(1)
                RepeatMode.MONTHLY -> next.plusMonths(1)
                else -> next
            }
        } while (next.toInstant().toEpochMilli() <= completedAt)

        return next.toInstant().toEpochMilli()
    }
}
