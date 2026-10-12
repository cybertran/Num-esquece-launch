package com.cybertran.numesquece.sync

import com.cybertran.numesquece.data.ReminderEntity
import org.json.JSONArray
import org.json.JSONObject

internal object SyncJsonCodec {
    private const val SCHEMA_VERSION = 1

    fun encode(reminders: List<ReminderEntity>): String {
        val items = JSONArray()
        reminders.forEach { reminder ->
            items.put(
                JSONObject()
                    .put("syncId", reminder.syncId)
                    .put("title", reminder.title)
                    .put("notes", reminder.notes)
                    .put("dueAt", reminder.dueAt)
                    .putNullable("windowEndAt", reminder.windowEndAt)
                    .put("intensity", reminder.intensity)
                    .put("repeatMode", reminder.repeatMode)
                    .put("repeatIntervalDays", reminder.repeatIntervalDays)
                    .put("status", reminder.status)
                    .put("snoozeCount", reminder.snoozeCount)
                    .put("createdAt", reminder.createdAt)
                    .putNullable("completedAt", reminder.completedAt)
                    .put("updatedAt", reminder.updatedAt)
            )
        }

        return JSONObject()
            .put("schema", SCHEMA_VERSION)
            .put("generatedAt", System.currentTimeMillis())
            .put("reminders", items)
            .toString()
    }

    fun decode(payload: String): List<ReminderEntity> {
        if (payload.isBlank()) return emptyList()
        val root = JSONObject(payload)
        val schema = root.optInt("schema", SCHEMA_VERSION)
        require(schema == SCHEMA_VERSION) { "Versão de sincronização incompatível: " + schema }

        val items = root.optJSONArray("reminders") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val syncId = item.optString("syncId")
                if (syncId.isBlank()) continue

                add(
                    ReminderEntity(
                        id = 0,
                        title = item.optString("title"),
                        notes = item.optString("notes"),
                        dueAt = item.optLong("dueAt"),
                        windowEndAt = item.optNullableLong("windowEndAt"),
                        intensity = item.optString("intensity", "NORMAL"),
                        repeatMode = item.optString("repeatMode", "NONE"),
                        repeatIntervalDays = item.optInt("repeatIntervalDays", 30),
                        status = item.optString("status", "ACTIVE"),
                        snoozeCount = item.optInt("snoozeCount", 0),
                        createdAt = item.optLong("createdAt"),
                        completedAt = item.optNullableLong("completedAt"),
                        syncId = syncId,
                        updatedAt = item.optLong("updatedAt"),
                    )
                )
            }
        }
    }

    private fun JSONObject.putNullable(key: String, value: Long?): JSONObject =
        if (value == null) put(key, JSONObject.NULL) else put(key, value)

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)
}
