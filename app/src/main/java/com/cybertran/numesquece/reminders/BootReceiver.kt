package com.cybertran.numesquece.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cybertran.numesquece.NumEsqueceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as NumEsqueceApplication
                app.repository.rescheduleActive()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
