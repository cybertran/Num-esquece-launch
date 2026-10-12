package com.cybertran.numesquece.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.cybertran.numesquece.R
import com.cybertran.numesquece.SettingsStore
import com.cybertran.numesquece.data.ReminderRepository
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class GoogleDriveSyncManager(
    context: Context,
    private val repository: ReminderRepository,
    private val settings: SettingsStore,
) {
    private val appContext = context.applicationContext
    private val authorizationClient = Identity.getAuthorizationClient(appContext)
    private val driveClient = DriveAppDataClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val syncMutex = Mutex()
    private val requestedScopes = listOf(Scope(DRIVE_APPDATA_SCOPE))

    private val _state = MutableStateFlow(
        GoogleSyncState(
            connected = settings.googleSyncEnabled,
            phase = if (settings.googleSyncEnabled) SyncPhase.IDLE else SyncPhase.DISCONNECTED,
            lastSyncAt = settings.lastGoogleSyncAt.takeIf { it > 0L },
        )
    )
    val state: StateFlow<GoogleSyncState> = _state.asStateFlow()

    fun connect(activity: Activity) {
        _state.value = _state.value.copy(
            phase = SyncPhase.CONNECTING,
            message = null,
        )
        requestAuthorization(activity, selectAccount = true)
    }

    fun syncNow(activity: Activity) {
        if (!settings.googleSyncEnabled) {
            connect(activity)
            return
        }
        _state.value = _state.value.copy(
            phase = SyncPhase.SYNCING,
            message = null,
        )
        requestAuthorization(activity, selectAccount = false)
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ): Boolean {
        if (requestCode != AUTHORIZATION_REQUEST_CODE) return false

        if (resultCode != Activity.RESULT_OK || data == null) {
            _state.value = _state.value.copy(
                phase = if (settings.googleSyncEnabled) SyncPhase.IDLE else SyncPhase.DISCONNECTED,
                message = appContext.getString(R.string.sync_connection_not_completed),
            )
            return true
        }

        val result = runCatching {
            authorizationClient.getAuthorizationResultFromIntent(data)
        }.getOrElse { error ->
            setError(appContext.getString(R.string.sync_authorization_not_completed), error)
            return true
        }

        consumeAuthorization(result)
        return true
    }

    fun disconnect() {
        settings.googleSyncEnabled = false
        GoogleSyncWork.cancel(appContext)
        _state.value = GoogleSyncState(
            connected = false,
            phase = SyncPhase.DISCONNECTED,
            lastSyncAt = settings.lastGoogleSyncAt.takeIf { it > 0L },
        )

        val request = RevokeAccessRequest.builder()
            .setScopes(requestedScopes)
            .build()

        authorizationClient.revokeAccess(request)
            .addOnFailureListener {
                _state.value = _state.value.copy(
                    message = appContext.getString(R.string.sync_revocation_failed),
                )
            }
    }

    suspend fun syncSilently(): SilentSyncResult {
        if (!settings.googleSyncEnabled) return SilentSyncResult.DISABLED

        return try {
            val result = authorizationClient
                .authorize(authorizationRequest(selectAccount = false))
                .await()

            if (result.hasResolution()) {
                _state.value = _state.value.copy(
                    connected = true,
                    phase = SyncPhase.ERROR,
                    message = appContext.getString(R.string.sync_reconfirm_account),
                )
                SilentSyncResult.NEEDS_USER
            } else {
                val token = result.accessToken
                if (token.isNullOrBlank()) {
                    SilentSyncResult.NEEDS_USER
                } else {
                    performSync(token)
                    SilentSyncResult.SUCCESS
                }
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(
                connected = true,
                phase = SyncPhase.ERROR,
                message = friendlyMessage(error),
            )
            SilentSyncResult.RETRY
        }
    }

    private fun requestAuthorization(
        activity: Activity,
        selectAccount: Boolean,
    ) {
        authorizationClient.authorize(authorizationRequest(selectAccount))
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent == null) {
                        setError(appContext.getString(R.string.sync_no_authorization_screen))
                        return@addOnSuccessListener
                    }

                    runCatching {
                        activity.startIntentSenderForResult(
                            pendingIntent.intentSender,
                            AUTHORIZATION_REQUEST_CODE,
                            null,
                            0,
                            0,
                            0,
                        )
                    }.onFailure { error ->
                        setError(appContext.getString(R.string.sync_open_authorization_failed), error)
                    }
                } else {
                    consumeAuthorization(result)
                }
            }
            .addOnFailureListener { error ->
                setError(
                    appContext.getString(R.string.sync_connection_failed),
                    error,
                )
            }
    }

    private fun consumeAuthorization(result: AuthorizationResult) {
        val token = result.accessToken
        if (token.isNullOrBlank()) {
            setError(appContext.getString(R.string.sync_invalid_authorization))
            return
        }

        settings.googleSyncEnabled = true
        GoogleSyncWork.ensurePeriodic(appContext)
        _state.value = _state.value.copy(
            connected = true,
            phase = SyncPhase.SYNCING,
            message = null,
        )

        scope.launch {
            runCatching { performSync(token) }
                .onFailure { error ->
                    setError(appContext.getString(R.string.sync_drive_failed), error)
                }
        }
    }

    private suspend fun performSync(accessToken: String) {
        syncMutex.withLock {
            val total = withContext(Dispatchers.IO) {
                val local = repository.snapshotForSync()
                val remoteSnapshot = driveClient.downloadLatest(accessToken)
                val remote = remoteSnapshot.payload
                    ?.let(SyncJsonCodec::decode)
                    .orEmpty()

                repository.mergeFromSync(remote)
                val merged = repository.snapshotForSync()

                driveClient.uploadSnapshot(
                    accessToken = accessToken,
                    payload = SyncJsonCodec.encode(merged),
                    existingFileIds = remoteSnapshot.existingFileIds,
                )
                merged.count { it.status != "DELETED" }
            }

            val now = System.currentTimeMillis()
            settings.lastGoogleSyncAt = now
            _state.value = GoogleSyncState(
                connected = true,
                phase = SyncPhase.IDLE,
                lastSyncAt = now,
                message = appContext.resources.getQuantityString(R.plurals.sync_reminders, total, total),
            )
        }
    }

    private fun authorizationRequest(selectAccount: Boolean): AuthorizationRequest {
        val builder = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)

        if (selectAccount) {
            builder.setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
        }
        return builder.build()
    }

    private fun setError(message: String, error: Throwable? = null) {
        _state.value = _state.value.copy(
            connected = settings.googleSyncEnabled,
            phase = SyncPhase.ERROR,
            message = if (error == null) message else message + " " + friendlyMessage(error),
        )
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is IOException -> appContext.getString(R.string.sync_check_connection)
        is ApiException -> when (error.statusCode) {
            CommonStatusCodes.DEVELOPER_ERROR ->
                appContext.getString(R.string.sync_oauth_mismatch)
            CommonStatusCodes.NETWORK_ERROR ->
                appContext.getString(R.string.sync_google_unreachable)
            CommonStatusCodes.CANCELED ->
                appContext.getString(R.string.sync_authorization_cancelled)
            CommonStatusCodes.INTERNAL_ERROR ->
                appContext.getString(R.string.sync_google_internal_error)
            else ->
                appContext.getString(R.string.sync_google_code, error.statusCode)
        }
        else -> appContext.getString(R.string.sync_try_again)
    }

    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }

    companion object {
        const val AUTHORIZATION_REQUEST_CODE = 7104
        private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }
}
