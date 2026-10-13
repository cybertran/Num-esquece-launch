package com.cybertran.numesquece

import android.app.Application
import com.cybertran.numesquece.data.ReminderDatabase
import com.cybertran.numesquece.data.ReminderRepository
import com.cybertran.numesquece.reminders.ReminderNotifications
import com.cybertran.numesquece.reminders.ReminderScheduler

class NumEsqueceApplication : Application() {
    val database by lazy { ReminderDatabase.create(this) }
    val scheduler by lazy { ReminderScheduler(this) }
    val settings by lazy { SettingsStore(this) }
    val repository by lazy {
        ReminderRepository(
            dao = database.reminderDao(),
            scheduler = scheduler,
        )
    }

    override fun onCreate() {
        super.onCreate()
        ReminderNotifications.createChannels(this)
    }
}
