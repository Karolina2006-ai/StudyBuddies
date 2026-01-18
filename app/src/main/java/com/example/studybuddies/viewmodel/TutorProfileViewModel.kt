package com.example.studybuddies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.User
import com.example.studybuddies.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the various states of the Tutor Profile screen.
 * Using a sealed class ensures the UI handles all scenarios (Loading, Success, Error)
 * and prevents invalid states.
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val tutor: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class TutorProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Internal mutable state flow to track the profile state
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)

    // External read-only state flow exposed to the UI (Compose/Fragments)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Fetches a tutor's profile details from the repository based on their unique ID.
     */
    fun loadTutor(tutorId: String) {
        viewModelScope.launch {
            // Set state to Loading whenever a new request starts
            _uiState.value = ProfileUiState.Loading
            try {
                // Perform the network/database fetch
                val tutor = userRepository.getUserProfile(tutorId)

                if (tutor != null) {
                    // Successfully retrieved data -> transition to Success state
                    _uiState.value = ProfileUiState.Success(tutor)
                } else {
                    // Result was null -> show a specific "Not Found" error
                    _uiState.value = ProfileUiState.Error("Tutor profile not found.")
                }
            } catch (e: Exception) {
                // Catch network issues or parsing errors and show the error state
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}