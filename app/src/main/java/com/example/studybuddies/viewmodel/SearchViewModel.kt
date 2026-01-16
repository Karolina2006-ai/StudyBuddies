package com.example.studybuddies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.studybuddies.data.model.User
import com.example.studybuddies.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SearchUiState(
    val searchQuery: String = "",
    val allTutors: List<User> = emptyList(),      // Technical base of valid tutors
    val filteredTutors: List<User> = emptyList(), // List rendered in the UI
    val recentTutors: List<User> = emptyList(),
    val showFilters: Boolean = false,
    val selectedPriceRange: String = "All prices",
    val selectedMode: String = "All",
    val selectedAvailability: String = "All",
    val selectedLocation: String = "",
    val currentUserUid: String? = null
)

class SearchViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadTutorsRealtime()
    }

    private fun loadTutorsRealtime() {
        val currentUid = auth.currentUser?.uid

        firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("SearchVM", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allUsers = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }

                    // --- STEP 1: Restrictive Filtering (Tutors only) ---
                    val validTutors = allUsers.filter { user ->
                        val roleClean = user.role.trim()
                        val isRoleTutor = roleClean.equals("Tutor", ignoreCase = true)
                        val isNotMe = if (currentUid != null) user.uid != currentUid else true

                        isRoleTutor && isNotMe
                    }

                    // --- STEP 2: Update database state in ViewModel ---
                    _uiState.update { currentState ->
                        currentState.copy(
                            allTutors = validTutors,
                            currentUserUid = currentUid
                        )
                    }

                    // --- STEP 3: Immediate Filter Recalculation ---
                    // Ensures filteredTutors is never empty if tutors exist in the DB.
                    applyFilters()
                }
            }
    }

    fun getTutorById(uid: String): User? {
        val tutor = _uiState.value.allTutors.find { it.uid == uid }
        if (tutor != null) {
            addToRecent(tutor)
        }
        return tutor
    }

    private fun addToRecent(tutor: User) {
        if (tutor.uid == _uiState.value.currentUserUid) return

        _uiState.update { state ->
            val updatedRecent = (listOf(tutor) + state.recentTutors)
                .distinctBy { it.uid }
                .take(5)
            state.copy(recentTutors = updatedRecent)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    fun updateFilters(price: String, mode: String, availability: String, location: String) {
        _uiState.update {
            it.copy(
                selectedPriceRange = price,
                selectedMode = mode,
                selectedAvailability = availability,
                selectedLocation = location,
                showFilters = false
            )
        }
        applyFilters()
    }

    /**
     * Main Filtering Logic - Professional Class Standard.
     */
    private fun applyFilters() {
        val s = _uiState.value
        val query = s.searchQuery.trim().lowercase()

        val filtered = s.allTutors.filter { tutor ->
            // 1. Multi-Field Search (First Name, Surname, Full Name, Subjects)
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                val firstNameMatch = tutor.firstName?.lowercase()?.contains(query) ?: false
                val surnameMatch = tutor.surname?.lowercase()?.contains(query) ?: false
                val fullNameMatch = tutor.fullName.lowercase().contains(query)
                val subjectMatch = tutor.subjects.any { it.lowercase().contains(query) }

                firstNameMatch || surnameMatch || fullNameMatch || subjectMatch
            }

            // 2. Price Filtering
            val matchesPrice = when (s.selectedPriceRange) {
                "Under 50 PLN" -> tutor.hourlyRate < 50
                "50 - 100 PLN" -> tutor.hourlyRate in 50.0..100.0
                "100+ PLN" -> tutor.hourlyRate > 100
                else -> true
            }

            // 3. Location Filtering
            val matchesLocation = s.selectedLocation.isBlank() ||
                    s.selectedLocation == "All locations" ||
                    tutor.city.contains(s.selectedLocation, ignoreCase = true)

            matchesSearch && matchesPrice && matchesLocation
        }

        // Final UI list update
        _uiState.update { it.copy(filteredTutors = filtered) }
    }
}