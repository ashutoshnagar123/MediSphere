package com.example.domain

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>

    suspend fun signUp(email: String, password: String, name: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    suspend fun updateProfile(user: User): AuthResult
    suspend fun logout()
}
