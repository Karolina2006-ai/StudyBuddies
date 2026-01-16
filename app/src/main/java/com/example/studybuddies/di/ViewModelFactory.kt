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
    private val application: Application, // 1. DODANO: Aplikacja jest teraz wymagana przez LessonsViewModel
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // We pass both repositories to AuthViewModel (login + profile fetching)
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, userRepository) as T
            }
            // We pass UserRepository to HomeViewModel (tutor lists, lessons)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(userRepository) as T
            }
            // We pass UserRepository to SearchViewModel (search, filters)
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(userRepository) as T
            }
            // 2. NAPRAWA: LessonsViewModel teraz wymaga 'application' na pierwszym miejscu
            modelClass.isAssignableFrom(LessonsViewModel::class.java) -> {
                LessonsViewModel(application, userRepository, FirebaseAuth.getInstance()) as T
            }
            // FIX: ChatViewModel now requires UserRepository
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(userRepository) as T
            }
            // Dodaję TutorProfileViewModel, żebyś nie miała błędu przy wchodzeniu w profil tutora
            modelClass.isAssignableFrom(TutorProfileViewModel::class.java) -> {
                TutorProfileViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}