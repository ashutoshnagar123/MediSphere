package com.example.data

import android.content.Context
import com.example.domain.AuthRepository
import com.example.domain.AuthResult
import com.example.domain.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(private val context: Context) : AuthRepository {

    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }
    
    private val auth by lazy { if (isFirebaseInitialized) FirebaseAuth.getInstance() else null }
    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        if (isFirebaseInitialized) {
            val fbUser = auth?.currentUser
            if (fbUser != null) {
                // Initialize temporarily, then fetch from firestore
                _currentUser.value = User(fbUser.uid, fbUser.email ?: "", fbUser.displayName ?: "User")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val doc = firestore?.collection("users")?.document(fbUser.uid)?.get()?.await()
                        if (doc != null && doc.exists()) {
                            val name = doc.getString("name") ?: fbUser.displayName ?: "User"
                            val age = doc.getString("age")
                            val gender = doc.getString("gender")
                            val bloodGroup = doc.getString("bloodGroup")
                            _currentUser.value = User(fbUser.uid, fbUser.email ?: "", name, age, gender, bloodGroup)
                        }
                    } catch (e: Exception) {
                        // Keep the default user initialized above
                    }
                }
            }
        } else {
             _currentUser.value = null
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): AuthResult {
        if (!isFirebaseInitialized) {
            _currentUser.value = User("mock_id", email, name)
            return AuthResult.Success
        }

        return try {
            val authResult = auth?.createUserWithEmailAndPassword(email, password)?.await()
            val userId = authResult?.user?.uid ?: return AuthResult.Error("Sign up failed")
            
            val userMap = mapOf(
                "userId" to userId,
                "email" to email,
                "name" to name
            )
            firestore?.collection("users")?.document(userId)?.set(userMap)?.await()
            
            _currentUser.value = User(userId, email, name)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        if (!isFirebaseInitialized) {
             if (email.contains("@") && password.length >= 6) {
                 _currentUser.value = User("mock_id", email, "Guest User")
                 return AuthResult.Success
             }
             return AuthResult.Error("Invalid mock credentials. Need valid email and 6+ chars password.")
        }

        return try {
            val authResult = auth?.signInWithEmailAndPassword(email, password)?.await()
            val fbUser = authResult?.user ?: return AuthResult.Error("Login failed")
            
            var name = "User"
            var age: String? = null
            var gender: String? = null
            var bloodGroup: String? = null
            try {
                val doc = firestore?.collection("users")?.document(fbUser.uid)?.get()?.await()
                if (doc != null && doc.exists()) {
                    name = doc.getString("name") ?: "User"
                    age = doc.getString("age")
                    gender = doc.getString("gender")
                    bloodGroup = doc.getString("bloodGroup")
                }
            } catch (e: Exception) {
                // Ignore failure
            }

            _currentUser.value = User(fbUser.uid, email, name, age, gender, bloodGroup)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Invalid email or password")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        if (!isFirebaseInitialized) {
             _currentUser.value = User("mock_google_id", "google@example.com", "Google User")
             return AuthResult.Success
        }
        return AuthResult.Error("Not configured in environment without keys")
    }

    override suspend fun resetPassword(email: String): AuthResult {
         if (!isFirebaseInitialized) return AuthResult.Success
         
         return try {
             auth?.sendPasswordResetEmail(email)?.await()
             AuthResult.Success
         } catch (e: Exception) {
             AuthResult.Error(e.message ?: "Failed to send reset email")
         }
    }

    override suspend fun updateProfile(user: User): AuthResult {
        if (!isFirebaseInitialized) {
            _currentUser.value = user
            return AuthResult.Success
        }

        return try {
            val userMap = mutableMapOf<String, Any>(
                "name" to user.name,
                "email" to user.email
            )
            user.age?.let { userMap["age"] = it }
            user.gender?.let { userMap["gender"] = it }
            user.bloodGroup?.let { userMap["bloodGroup"] = it }

            firestore?.collection("users")?.document(user.id)?.update(userMap)?.await()
            _currentUser.value = user
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun logout() {
        if (isFirebaseInitialized) {
            auth?.signOut()
        }
        _currentUser.value = null
    }
}
