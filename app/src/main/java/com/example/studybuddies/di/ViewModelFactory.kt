package com.example.studybuddies.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studybuddies.data.repository.AuthRepository
import com.example.studybuddies.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel Factory.
 * Responsible for creating instances of ViewModels and passing the appropriate Repositories to them.
 */
class AppViewModelFactory(
    private val application: Application, // Used for context-dependent features like notifications
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // Tells the compiler: "I know what I'm doing, don't worry about the type safety here."
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // The 'when' block acts like a router: it checks which ViewModel is being requested.
        return when {
            // AuthViewModel needs both because it handles the Auth account AND the Firestore profile.
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, userRepository) as T
            }
            // Standard data-driven ViewModels only need the UserRepository.
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(userRepository) as T
            }
            /**
             * Special case: LessonsViewModel.
             * It needs the 'application' instance because it probably handles
             * logic that requires a Context (like checking system time or notifications).
             */
            modelClass.isAssignableFrom(LessonsViewModel::class.java) -> {
                LessonsViewModel(application, userRepository, FirebaseAuth.getInstance()) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(TutorProfileViewModel::class.java) -> {
                TutorProfileViewModel(userRepository) as T
            }
            // If we try to inject a ViewModel that isn't listed here, the app will crash with an error.
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}