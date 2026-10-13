package com.cybertran.numesquece.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueAt ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE status = 'ACTIVE' ORDER BY dueAt ASC")
    suspend fun active(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): ReminderEntity?

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM reminders WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}

@Database(entities = [ReminderEntity::class], version = 3, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        private fun migrateToOfflineSchema(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reminders_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    notes TEXT NOT NULL,
                    dueAt INTEGER NOT NULL,
                    deadlineAt INTEGER,
                    deadlineOffsetMinutes INTEGER,
                    intensity TEXT NOT NULL,
                    repeatMode TEXT NOT NULL,
                    repeatIntervalDays INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    snoozeCount INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    completedAt INTEGER,
                    monthlyAnchorDay INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO reminders_new (
                    id, title, notes, dueAt, deadlineAt, deadlineOffsetMinutes,
                    intensity, repeatMode, repeatIntervalDays, status,
                    snoozeCount, createdAt, completedAt, monthlyAnchorDay
                )
                SELECT
                    id, title, notes, dueAt, windowEndAt,
                    CASE
                        WHEN windowEndAt IS NULL OR windowEndAt <= dueAt THEN NULL
                        ELSE CAST((windowEndAt - dueAt) / 60000 AS INTEGER)
                    END,
                    intensity, repeatMode, repeatIntervalDays, status,
                    snoozeCount, createdAt, completedAt, NULL
                FROM reminders
                WHERE status != 'DELETED'
                """.trimIndent()
            )
            db.execSQL("DROP TABLE reminders")
            db.execSQL("ALTER TABLE reminders_new RENAME TO reminders")
        }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateToOfflineSchema(db)
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateToOfflineSchema(db)
        }

        fun create(context: Context): ReminderDatabase = Room.databaseBuilder(
            context.applicationContext,
            ReminderDatabase::class.java,
            "num-esquece.db",
        )
            .addMigrations(MIGRATION_1_3, MIGRATION_2_3)
            .build()
    }
}
