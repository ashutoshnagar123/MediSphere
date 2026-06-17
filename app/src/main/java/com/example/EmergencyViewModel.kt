package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.EmergencyContact
import com.example.domain.EmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val emergencyRepository: EmergencyRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts.asStateFlow()

    fun loadContacts(userId: String) {
        viewModelScope.launch {
            emergencyRepository.getContacts(userId).collect {
                _contacts.value = it
            }
        }
    }

    fun addContact(userId: String, contact: EmergencyContact) {
        viewModelScope.launch {
            emergencyRepository.addContact(userId, contact)
        }
    }

    fun updateContact(userId: String, contact: EmergencyContact) {
        viewModelScope.launch {
            emergencyRepository.updateContact(userId, contact)
        }
    }

    fun deleteContact(userId: String, contactId: String) {
        viewModelScope.launch {
            emergencyRepository.deleteContact(userId, contactId)
        }
    }

    companion object {
        fun provideFactory(
            emergencyRepo: EmergencyRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EmergencyViewModel(emergencyRepo) as T
            }
        }
    }
}
