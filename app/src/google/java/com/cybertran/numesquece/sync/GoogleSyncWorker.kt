package com.cybertran.numesquece.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cybertran.numesquece.NumEsqueceApplication
import com.cybertran.numesquece.SettingsStore
import java.util.concurrent.TimeUnit

class GoogleSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as NumEsqueceApplication
        return when (app.googleSyncManager.syncSilently()) {
            SilentSyncResult.SUCCESS,
            SilentSyncResult.DISABLED,
            SilentSyncResult.NEEDS_USER -> Result.success()

            SilentSyncResult.RETRY -> Result.retry()
        }
    }
}

object GoogleSyncWork {
    private const val ONE_TIME_NAME = "num-esquece-google-sync-now"
    private const val PERIODIC_NAME = "num-esquece-google-sync-periodic"

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueueIfEnabled(context: Context, settings: SettingsStore) {
        if (!settings.googleSyncEnabled) return

        val request = OneTimeWorkRequestBuilder<GoogleSyncWorker>()
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<GoogleSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(ONE_TIME_NAME)
        workManager.cancelUniqueWork(PERIODIC_NAME)
    }
}
