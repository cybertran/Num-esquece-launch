package com.cybertran.numesquece.sync

import android.content.Context
import com.cybertran.numesquece.SettingsStore

object GoogleSyncWork {
    fun enqueueIfEnabled(context: Context, settings: SettingsStore) = Unit
    fun ensurePeriodic(context: Context) = Unit
    fun cancel(context: Context) = Unit
}
