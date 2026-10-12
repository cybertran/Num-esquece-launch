package com.cybertran.numesquece.sync

enum class SyncPhase {
    DISCONNECTED,
    IDLE,
    CONNECTING,
    SYNCING,
    ERROR,
}

data class GoogleSyncState(
    val connected: Boolean,
    val phase: SyncPhase,
    val lastSyncAt: Long? = null,
    val message: String? = null,
)

enum class SilentSyncResult {
    SUCCESS,
    DISABLED,
    NEEDS_USER,
    RETRY,
}
