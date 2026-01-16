package com.example.studybuddies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.User
import com.example.studybuddies.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// View States (Loading, Success, Error)
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val tutor: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class TutorProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadTutor(tutorId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Fetch tutor data from the repository
                val tutor = userRepository.getUserProfile(tutorId)

                if (tutor != null) {
                    // Data found -> Show profile
                    _uiState.value = ProfileUiState.Success(tutor)
                } else {
                    // Not found in database -> Show error instead of loading infinitely
                    _uiState.value = ProfileUiState.Error("Tutor profile not found.")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}