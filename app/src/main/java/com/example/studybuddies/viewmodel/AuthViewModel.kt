package com.example.studybuddies.viewmodel

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

data class AuthState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val error: String? = null,
    val isPasswordResetSent: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            val firebaseUser = authRepository.getCurrentUser()

            if (firebaseUser != null) {
                try {
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

    fun clearResetState() {
        _authState.update { it.copy(isPasswordResetSent = false, error = null) }
    }

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

    fun updateUserProfile(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            try {
                userRepository.saveUserProfile(user)
                _authState.update { it.copy(currentUser = user, isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * "OUT OF THE BOX" REGISTRATION:
     * We use HashMap and ArrayList instead of emptyMap/emptyList to ensure Firebase does not crash on serialization.
     * We also enforce Dispatchers.IO for thread safety.
     */
    suspend fun register(
        email: String, pass: String, firstName: String, surname: String,
        role: String, city: String, university: String, telephone: String,
        bio: String, hobbies: List<String>, subjects: List<String>
    ): Boolean {
        _authState.update { it.copy(isLoading = true, error = null) }

        // 1. Register in Auth
        val result = authRepository.register(
            email = email,
            pass = pass,
            firstName = firstName,
            surname = surname,
            role = role,
            city = city,
            university = university,
            phone = telephone,
            bio = bio,
            hobbies = hobbies,
            subjects = subjects
        )

        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()

            if (firebaseUser != null) {
                // 2. Create User object with type-safe collections (HashMap/ArrayList)
                // This prevents crashes when saving empty fields to Firestore.
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
                    // FIX: Convert generic List to ArrayList explicitly
                    hobbies = ArrayList(hobbies),
                    subjects = ArrayList(subjects),
                    hourlyRate = 50.0,
                    // FIX: Use hashMapOf() instead of emptyMap()
                    availability = hashMapOf(),
                    ratingStats = hashMapOf(
                        "5" to 0L, "4" to 0L, "3" to 0L, "2" to 0L, "1" to 0L
                    ),
                    // FIX: Use arrayListOf() instead of emptyList()
                    reviews = arrayListOf(),
                    totalReviews = 0,
                    averageRating = 0.0
                )

                try {
                    // 3. Save on IO thread (safe for network operations)
                    withContext(Dispatchers.IO) {
                        userRepository.saveUserProfile(user)
                    }

                    // 4. Stabilization delay
                    delay(1000)

                    // 5. Update state
                    _authState.update {
                        it.copy(
                            isAuthenticated = true,
                            currentUser = user,
                            uid = firebaseUser.uid,
                            isLoading = false
                        )
                    }
                    true
                } catch (e: Exception) {
                    _authState.update {
                        it.copy(isLoading = false, error = "Error saving profile: ${e.message}")
                    }
                    false
                }
            } else {
                _authState.update {
                    it.copy(isLoading = false, error = "UID Error.")
                }
                false
            }
        } else {
            _authState.update {
                it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
            false
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.update { AuthState() }
    }
}