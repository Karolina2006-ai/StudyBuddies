package com.example.studybuddies.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.User
import com.example.studybuddies.data.repository.AuthRepository
import com.example.studybuddies.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

// Represents the current state of authentication in the UI
data class AuthState(
    val isLoading: Boolean = false, // True when communicating with Firebase
    val currentUser: User? = null, // The profile data from Firestore
    val isAuthenticated: Boolean = false, // True if the user is successfully logged in
    val uid: String? = null, // The unique Firebase ID
    val error: String? = null, // Holds error messages for the student to see
    val isPasswordResetSent: Boolean = false // Flag for password recovery success
)

class AuthViewModel(
    private val authRepository: AuthRepository, // Handles Firebase Auth (Email/Pass)
    private val userRepository: UserRepository // Handles Firestore (Profile data) and Storage (Images)
) : ViewModel() {

    // Private mutable state and public immutable flow for UI observation
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        // Automatically check if the user is already logged in when the app starts
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            val firebaseUser = authRepository.getCurrentUser()

            if (firebaseUser != null) {
                try {
                    // Fetch profile with a 10-second timeout limit
                    withTimeout(10000) {
                        val profile = userRepository.getUserProfile(firebaseUser.uid)
                        if (profile != null) {
                            _authState.update {
                                it.copy(
                                    isAuthenticated = true,
                                    currentUser = profile,
                                    uid = firebaseUser.uid,
                                    isLoading = false
                                )
                            }
                        } else {
                            // If user exists in Auth but not in Firestore, clean up
                            authRepository.logout()
                            _authState.update {
                                it.copy(isAuthenticated = false, currentUser = null, uid = null, isLoading = false)
                            }
                        }
                    }
                } catch (e: Exception) {
                    _authState.update { it.copy(isLoading = false, error = "Connection error: ${e.message}") }
                }
            } else {
                _authState.update { it.copy(isLoading = false, isAuthenticated = false, uid = null) }
            }
        }
    }

    // Standard login flow: Authenticate then fetch profile
    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val res = authRepository.login(email, pass)

            if (res.isSuccess) {
                val uid = authRepository.getCurrentUser()?.uid
                if (uid != null) {
                    try {
                        withTimeout(10000) {
                            val profile = userRepository.getUserProfile(uid)
                            if (profile != null) {
                                _authState.update {
                                    it.copy(
                                        isAuthenticated = true,
                                        currentUser = profile,
                                        uid = uid,
                                        isLoading = false
                                    )
                                }
                            } else {
                                _authState.update { it.copy(isLoading = false, error = "Profile not found.") }
                            }
                        }
                    } catch (e: Exception) {
                        _authState.update { it.copy(isLoading = false, error = "Login timeout.") }
                    }
                }
            } else {
                _authState.update { it.copy(isLoading = false, error = res.exceptionOrNull()?.message) }
            }
        }
    }

    // Triggers Firebase password reset email
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authRepository.sendPasswordResetEmail(email)
                if (result.isFailure) {
                    _authState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                } else {
                    _authState.update { it.copy(isLoading = false, isPasswordResetSent = true, error = null) }
                }
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Resets the UI state for the password recovery screen
    fun clearResetState() {
        _authState.update { it.copy(isPasswordResetSent = false, error = null) }
    }

    // Force-refreshes the current user's profile data
    fun refreshUser() {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            try {
                val profile = userRepository.getUserProfile(uid)
                if (profile != null) {
                    _authState.update { it.copy(isAuthenticated = true, currentUser = profile, uid = uid) }
                }
            } catch (e: Exception) { }
        }
    }

    // Handles profile updates, including image upload to Storage
    fun updateUserProfile(user: User, newImageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                var finalUser = user

                // STEP 1: If a new image was selected, upload it to Firebase Storage first
                if (newImageUri != null) {
                    val downloadUrl = userRepository.uploadProfileImage(user.uid, newImageUri)
                    // Replace local URI with the public download URL
                    finalUser = user.copy(profileImageUri = downloadUrl)
                }

                // STEP 2: Save the updated user object to Firestore
                userRepository.saveUserProfile(finalUser)

                // STEP 3: Update local state to reflect changes instantly in the UI
                _authState.update { it.copy(currentUser = finalUser, isLoading = false) }

                onSuccess()
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Registration flow: Creates Auth account then initializes Firestore document
    suspend fun register(
        email: String, pass: String, firstName: String, surname: String,
        role: String, city: String, university: String, telephone: String,
        bio: String, hobbies: List<String>, subjects: List<String>
    ): Boolean {
        _authState.update { it.copy(isLoading = true, error = null) }

        val result = authRepository.register(
            email = email, pass = pass, firstName = firstName, surname = surname,
            role = role, city = city, university = university, phone = telephone,
            bio = bio, hobbies = hobbies, subjects = subjects
        )

        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()

            if (firebaseUser != null) {
                // Constructing the default User object for Firestore
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    firstName = firstName,
                    surname = surname,
                    fullName = "$firstName $surname",
                    role = role,
                    city = city,
                    university = university,
                    telephone = telephone,
                    bio = bio,
                    hobbies = ArrayList(hobbies),
                    subjects = ArrayList(subjects),
                    hourlyRate = 50.0, // Default price
                    availability = hashMapOf(),
                    ratingStats = hashMapOf("5" to 0L, "4" to 0L, "3" to 0L, "2" to 0L, "1" to 0L),
                    reviews = arrayListOf(),
                    totalReviews = 0,
                    averageRating = 0.0
                )

                try {
                    // Switch to IO thread for network database operation
                    withContext(Dispatchers.IO) {
                        userRepository.saveUserProfile(user)
                    }
                    delay(1000) // Small delay to ensure Firebase propagation
                    _authState.update {
                        it.copy(isAuthenticated = true, currentUser = user, uid = firebaseUser.uid, isLoading = false)
                    }
                    true
                } catch (e: Exception) {
                    _authState.update { it.copy(isLoading = false, error = "Error saving profile: ${e.message}") }
                    false
                }
            } else {
                _authState.update { it.copy(isLoading = false, error = "UID Error.") }
                false
            }
        } else {
            _authState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            false
        }
    }

    // Clears Auth and resets local state to default
    fun logout() {
        authRepository.logout()
        _authState.update { AuthState() }
    }
}