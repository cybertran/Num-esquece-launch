package com.cybertran.numesquece

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cybertran.numesquece.data.ReminderEntity
import com.cybertran.numesquece.data.ReminderIntensity
import com.cybertran.numesquece.data.ReminderRepository
import com.cybertran.numesquece.data.RepeatMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
) : ViewModel() {
    val reminders = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(reminder: ReminderEntity, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.save(reminder)
            onSaved?.invoke()
        }
    }

    fun complete(id: Long) {
        viewModelScope.launch { repository.complete(id) }
    }

    fun snooze(id: Long, minutes: Int) {
        viewModelScope.launch { repository.snooze(id, minutes) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun clearAll(onCleared: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAll()
            onCleared?.invoke()
        }
    }

    fun createTestReminder(title: String, notes: String) {
        val now = System.currentTimeMillis()
        save(
            ReminderEntity(
                title = title,
                notes = notes,
                dueAt = now + 60_000L,
                intensity = ReminderIntensity.NORMAL.name,
                repeatMode = RepeatMode.NONE.name,
            )
        )
    }

    class Factory(private val app: NumEsqueceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderViewModel(app.repository) as T
        }
    }
}
