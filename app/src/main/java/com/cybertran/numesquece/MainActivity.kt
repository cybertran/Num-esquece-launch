package com.cybertran.numesquece

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cybertran.numesquece.ui.NumEsqueceRoot
import kotlinx.coroutines.launch

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
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as NumEsqueceApplication
        lifecycleScope.launch {
            app.repository.rescheduleActive()
        }
    }
}
