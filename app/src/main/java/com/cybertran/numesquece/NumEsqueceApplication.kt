package com.cybertran.numesquece

import android.app.Application
import com.cybertran.numesquece.data.ReminderDatabase
import com.cybertran.numesquece.data.ReminderRepository
import com.cybertran.numesquece.reminders.ReminderNotifications
import com.cybertran.numesquece.reminders.ReminderScheduler
import com.cybertran.numesquece.sync.GoogleDriveSyncManager
import com.cybertran.numesquece.sync.GoogleSyncWork

class NumEsqueceApplication : Application() {
    val database by lazy { ReminderDatabase.create(this) }
    val scheduler by lazy { ReminderScheduler(this) }
    val settings by lazy { SettingsStore(this) }
    val repository by lazy {
        ReminderRepository(
            dao = database.reminderDao(),
            scheduler = scheduler,
            onChanged = {
                if (BuildConfig.GOOGLE_SYNC_AVAILABLE) {
                    GoogleSyncWork.enqueueIfEnabled(this, settings)
                }
            },
        )
    }
    val googleSyncManager by lazy {
        GoogleDriveSyncManager(
            context = this,
            repository = repository,
            settings = settings,
        )
    }

    override fun onCreate() {
        super.onCreate()
        ReminderNotifications.createChannels(this)
        if (BuildConfig.GOOGLE_SYNC_AVAILABLE && settings.googleSyncEnabled) {
            GoogleSyncWork.ensurePeriodic(this)
        } else if (!BuildConfig.GOOGLE_SYNC_AVAILABLE) {
            GoogleSyncWork.cancel(this)
        }
    }
}
