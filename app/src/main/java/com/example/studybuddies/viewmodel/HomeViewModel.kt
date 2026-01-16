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

data class HomeUiState(
    val recommendedTutors: List<User> = emptyList(),
    val upcomingLessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = false,
    val userName: String = ""
)

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // --- STAN UI ---
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var lessonsListener: ListenerRegistration? = null

    // Strażnik sesji
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            // Uruchamiamy dwa NIEZALEŻNE procesy.
            // Jeśli jeden zwolni, drugi i tak zaktualizuje ekran.
            observeLessonsRealtime(uid)     // 1. Lekcje (Szybkie)
            loadUserProfileAndTutors(uid)   // 2. Tutorzy (Wolniejsi)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        // Próba startu na wypadek, gdyby user już był zalogowany
        auth.currentUser?.uid?.let { uid ->
            observeLessonsRealtime(uid)
            loadUserProfileAndTutors(uid)
        }
    }

    /**
     * PROCES 1: LEKCJE (Krytyczny dla Ciebie)
     * Aktualizuje UI natychmiast po otrzymaniu danych z bazy.
     * Nie czeka na nic innego.
     */
    private fun observeLessonsRealtime(uid: String) {
        lessonsListener?.remove()

        lessonsListener = firestore.collection("lessons")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeVM", "Snapshot Error", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allLessons = snapshot.documents.mapNotNull { it.toObject(Lesson::class.java) }

                    // FILTR 1:1 Z LESSONS VIEW MODEL (BEZPIECZNY)
                    // Bierzemy wszystko, co należy do użytkownika.
                    val myLessons = allLessons.filter { lesson ->
                        val isParticipant = (lesson.studentId == uid || lesson.tutorId == uid)
                        // Nie filtrujemy po dacie ani statusie, żeby mieć pewność, że się pojawi
                        val isActive = (lesson.status == "Confirmed" || lesson.status == "Upcoming" || lesson.status == "Pending")
                        isParticipant && isActive
                    }
                        .sortedBy { it.date }
                        .take(5)

                    // NATYCHMIASTOWA AKTUALIZACJA UI
                    _uiState.update {
                        it.copy(
                            upcomingLessons = myLessons,
                            isLoading = false // Wyłączamy loader, bo mamy lekcje!
                        )
                    }
                    Log.d("HomeVM", "Lekcje zaktualizowane: ${myLessons.size}")
                }
            }
    }

    /**
     * PROCES 2: DANE STATYCZNE (Tutorzy, Profil)
     * Działa w tle, nie blokuje lekcji.
     */
    private fun loadUserProfileAndTutors(uid: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val profile = userRepository.getUserProfile(uid)
                    val name = profile?.firstName ?: "Study Buddy"

                    val allUsers = userRepository.getAllTutors()
                    val filteredTutors = allUsers.filter { user ->
                        val roleClean = user.role.trim()
                        val isRoleTutor = roleClean.equals("Tutor", ignoreCase = true)
                        val isNotMe = user.uid != uid
                        isRoleTutor && isNotMe
                    }

                    // Aktualizacja UI niezależna od lekcji
                    _uiState.update {
                        it.copy(
                            userName = name,
                            recommendedTutors = filteredTutors
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Błąd profilu", e)
            } finally {
                // Upewniamy się, że loader znika
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            observeLessonsRealtime(uid)
            loadUserProfileAndTutors(uid)
        }
    }

    fun clearData() {
        lessonsListener?.remove()
        _uiState.value = HomeUiState(isLoading = true)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        lessonsListener?.remove()
    }
}