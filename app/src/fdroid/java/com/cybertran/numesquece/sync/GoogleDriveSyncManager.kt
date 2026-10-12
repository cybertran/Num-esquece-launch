package com.cybertran.numesquece.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.cybertran.numesquece.SettingsStore
import com.cybertran.numesquece.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleDriveSyncManager(
    context: Context,
    repository: ReminderRepository,
    settings: SettingsStore,
) {
    private val _state = MutableStateFlow(
        GoogleSyncState(
            connected = false,
            phase = SyncPhase.DISCONNECTED,
        )
    )
    val state: StateFlow<GoogleSyncState> = _state.asStateFlow()

    fun connect(activity: Activity) = Unit
    fun syncNow(activity: Activity) = Unit
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean = false
    fun disconnect() = Unit
    suspend fun syncSilently(): SilentSyncResult = SilentSyncResult.DISABLED
}
