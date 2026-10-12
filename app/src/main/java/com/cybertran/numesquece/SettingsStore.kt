package com.cybertran.numesquece

import android.content.Context
import androidx.annotation.StringRes

enum class NotificationSoundMode(@StringRes val labelRes: Int) {
    NUM_ESQUECE(R.string.sound_num_esquece),
    SYSTEM(R.string.sound_system),
    SILENT(R.string.sound_silent),
}

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("num_esquece_settings", Context.MODE_PRIVATE)

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()

    var notificationSoundMode: NotificationSoundMode
        get() = runCatching {
            NotificationSoundMode.valueOf(
                prefs.getString(KEY_NOTIFICATION_SOUND_MODE, NotificationSoundMode.NUM_ESQUECE.name)
                    ?: NotificationSoundMode.NUM_ESQUECE.name
            )
        }.getOrDefault(NotificationSoundMode.NUM_ESQUECE)
        set(value) = prefs.edit().putString(KEY_NOTIFICATION_SOUND_MODE, value.name).apply()

    var googleSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOOGLE_SYNC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_GOOGLE_SYNC_ENABLED, value).apply()

    var lastGoogleSyncAt: Long
        get() = prefs.getLong(KEY_LAST_GOOGLE_SYNC_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_GOOGLE_SYNC_AT, value).apply()

    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_NOTIFICATION_SOUND_MODE = "notification_sound_mode"
        private const val KEY_GOOGLE_SYNC_ENABLED = "google_sync_enabled"
        private const val KEY_LAST_GOOGLE_SYNC_AT = "last_google_sync_at"
    }
}
