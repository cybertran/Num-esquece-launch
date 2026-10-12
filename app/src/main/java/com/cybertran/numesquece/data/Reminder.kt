package com.cybertran.numesquece.data

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cybertran.numesquece.R
import java.util.UUID

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueAt: Long,
    val windowEndAt: Long? = null,
    val intensity: String = ReminderIntensity.NORMAL.name,
    val repeatMode: String = RepeatMode.NONE.name,
    val repeatIntervalDays: Int = 30,
    val status: String = ReminderStatus.ACTIVE.name,
    val snoozeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    @ColumnInfo(defaultValue = "''")
    val syncId: String = UUID.randomUUID().toString(),
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class ReminderIntensity(@StringRes val labelRes: Int, val repeatMinutesAfterDismiss: Int?) {
    NORMAL(R.string.intensity_normal, null),
    INSISTENT(R.string.intensity_insistent, 15),
    DONT_LET_ME_FORGET(R.string.intensity_dont_forget, 5),
}

enum class ReminderStatus {
    ACTIVE,
    COMPLETED,
    DELETED,
}

enum class RepeatMode(@StringRes val labelRes: Int) {
    NONE(R.string.repeat_none),
    DAILY(R.string.repeat_daily),
    WEEKLY(R.string.repeat_weekly),
    MONTHLY(R.string.repeat_monthly),
    AFTER_COMPLETION(R.string.repeat_after_completion),
}

fun ReminderEntity.intensityValue(): ReminderIntensity =
    runCatching { ReminderIntensity.valueOf(intensity) }.getOrDefault(ReminderIntensity.NORMAL)

fun ReminderEntity.repeatModeValue(): RepeatMode =
    runCatching { RepeatMode.valueOf(repeatMode) }.getOrDefault(RepeatMode.NONE)

fun ReminderEntity.statusValue(): ReminderStatus =
    runCatching { ReminderStatus.valueOf(status) }.getOrDefault(ReminderStatus.ACTIVE)
