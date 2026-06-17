package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.AuthRepository
import com.example.domain.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser = repository.currentUser

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.login(email.trim(), password)
            handleResult(result)
        }
    }

    fun signUp(email: String, password: String, name: String) {
        if (!validate(email, password) || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill out all fields correctly.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.signUp(email.trim(), password, name.trim())
            handleResult(result)
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.resetPassword(email.trim())
            if (result is AuthResult.Success) {
                _uiState.value = AuthUiState.Idle // Reset link sent
            } else if (result is AuthResult.Error) {
                _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun updateProfile(name: String, age: String, gender: String, bloodGroup: String) {
        val user = currentUser.value ?: return
        
        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Name cannot be empty")
            return
        }
        
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val updatedUser = user.copy(
                name = name.trim(),
                age = age.trim().takeIf { it.isNotBlank() },
                gender = gender.trim().takeIf { it.isNotBlank() },
                bloodGroup = bloodGroup.trim().takeIf { it.isNotBlank() }
            )
            val result = repository.updateProfile(updatedUser)
            handleResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Invalid email address")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.value = AuthUiState.Success
            }
            is AuthResult.Error -> {
                _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    companion object {
        fun provideFactory(repository: AuthRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    return AuthViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
