package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.MedicineReminder
import com.example.domain.ReminderHistoryItem
import com.example.domain.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<MedicineReminder>>(emptyList())
    val reminders: StateFlow<List<MedicineReminder>> = _reminders.asStateFlow()

    private val _history = MutableStateFlow<List<ReminderHistoryItem>>(emptyList())
    val history: StateFlow<List<ReminderHistoryItem>> = _history.asStateFlow()

    fun loadData(userId: String) {
        viewModelScope.launch {
            launch {
                reminderRepository.getReminders(userId).collect { list ->
                    _reminders.value = list
                }
            }
            launch {
                reminderRepository.getHistory(userId).collect { list ->
                    _history.value = list
                }
            }
        }
    }

    fun addReminder(userId: String, reminder: MedicineReminder) {
        viewModelScope.launch {
            reminderRepository.addReminder(userId, reminder)
        }
    }

    fun updateReminder(userId: String, reminder: MedicineReminder) {
        viewModelScope.launch {
            reminderRepository.updateReminder(userId, reminder)
        }
    }

    fun deleteReminder(userId: String, reminderId: String) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(userId, reminderId)
        }
    }

    fun addHistoryItem(userId: String, item: ReminderHistoryItem) {
        viewModelScope.launch {
            reminderRepository.addHistoryItem(userId, item)
        }
    }

    companion object {
        fun provideFactory(repository: ReminderRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                    return ReminderViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
