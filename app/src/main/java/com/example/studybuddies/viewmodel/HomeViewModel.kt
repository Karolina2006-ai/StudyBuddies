package com.example.studybuddies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.Lesson
import com.example.studybuddies.data.model.User
import com.example.studybuddies.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Defines the complete visual state of the Home Screen
data class HomeUiState(
    val recommendedTutors: List<User> = emptyList(), // List of suggested teachers
    val upcomingLessons: List<Lesson> = emptyList(), // The student's next scheduled sessions
    val isLoading: Boolean = false, // Controls the visibility of the loading spinner
    val userName: String = "" // Personalized greeting (e.g., "Hello, Mark!")
)

class HomeViewModel(
    private val userRepository: UserRepository // Handles database access for user/tutor profiles
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // --- UI STATE MANAGEMENT ---
    // Mutable internal state and immutable public flow for UI observation
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var lessonsListener: ListenerRegistration? = null // Reference to the active Firebase listener

    // Session Guardian: Reacts whenever the user logs in or out
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            // Trigger two INDEPENDENT processes.
            // If one is slow, the other still updates the screen immediately.
            observeLessonsRealtime(uid)     // 1. Lessons (Fast & Critical)
            loadUserProfileAndTutors(uid)   // 2. Tutors (Background/Static)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        // Check for existing session on startup
        auth.currentUser?.uid?.let { uid ->
            observeLessonsRealtime(uid)
            loadUserProfileAndTutors(uid)
        }
    }

    /**
     * PROCESS 1: REAL-TIME LESSONS
     * Updates the UI immediately upon receiving database changes.
     */
    private fun observeLessonsRealtime(uid: String) {
        lessonsListener?.remove() // Clean up any old listeners before starting a new one

        lessonsListener = firestore.collection("lessons")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeVM", "Snapshot Error", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Convert raw documents to Lesson objects
                    val allLessons = snapshot.documents.mapNotNull { it.toObject(Lesson::class.java) }

                    // Safety Filter: Only include lessons where the user is a participant
                    val myLessons = allLessons.filter { lesson ->
                        val isParticipant = (lesson.studentId == uid || lesson.tutorId == uid)
                        val isActive = (lesson.status == "Confirmed" || lesson.status == "Upcoming" || lesson.status == "Pending")
                        isParticipant && isActive
                    }
                        .sortedBy { it.date }
                        .take(5) // Only show the top 5 to keep the dashboard clean

                    // PUSH TO UI: Instantly updates the screen
                    _uiState.update {
                        it.copy(
                            upcomingLessons = myLessons,
                            isLoading = false // Turn off loader as soon as lessons arrive
                        )
                    }
                    Log.d("HomeVM", "Lessons updated: ${myLessons.size}")
                }
            }
    }

    /**
     * PROCESS 2: STATIC DATA (Tutors, Profile)
     * Runs in the background without blocking the lesson stream.
     */
    private fun loadUserProfileAndTutors(uid: String) {
        viewModelScope.launch {
            try {
                // Perform heavy database work on the IO thread
                withContext(Dispatchers.IO) {
                    val profile = userRepository.getUserProfile(uid)
                    val name = profile?.firstName ?: "Study Buddy"

                    val allUsers = userRepository.getAllTutors()
                    val filteredTutors = allUsers.filter { user ->
                        val roleClean = user.role.trim()
                        val isRoleTutor = roleClean.equals("Tutor", ignoreCase = true)
                        val isNotMe = user.uid != uid
                        isRoleTutor && isNotMe // Exclude current user from recommendations
                    }

                    // Update UI independently of the lesson stream
                    _uiState.update {
                        it.copy(
                            userName = name,
                            recommendedTutors = filteredTutors
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Profile error", e)
            } finally {
                // Ensure loader is hidden even if an error occurs
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Manual refresh trigger (e.g., Pull-to-refresh)
    fun refreshData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            observeLessonsRealtime(uid)
            loadUserProfileAndTutors(uid)
        }
    }

    // Clears the screen state when logging out
    fun clearData() {
        lessonsListener?.remove()
        _uiState.value = HomeUiState(isLoading = true)
    }

    override fun onCleared() {
        super.onCleared()
        // Critical: Detach listeners to prevent memory leaks
        auth.removeAuthStateListener(authStateListener)
        lessonsListener?.remove()
    }
}