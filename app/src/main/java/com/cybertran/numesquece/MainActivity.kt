package com.cybertran.numesquece

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.cybertran.numesquece.ui.NumEsqueceRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val app = application as NumEsqueceApplication
        val viewModel = ViewModelProvider(this, ReminderViewModel.Factory(app))[ReminderViewModel::class.java]

        setContent {
            NumEsqueceRoot(
                viewModel = viewModel,
                settings = app.settings,
                googleSyncManager = app.googleSyncManager,
                onConnectGoogle = { app.googleSyncManager.connect(this@MainActivity) },
                onSyncGoogle = { app.googleSyncManager.syncNow(this@MainActivity) },
                onDisconnectGoogle = app.googleSyncManager::disconnect,
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val app = application as NumEsqueceApplication
        if (app.googleSyncManager.handleActivityResult(requestCode, resultCode, data)) return
        super.onActivityResult(requestCode, resultCode, data)
    }
}
