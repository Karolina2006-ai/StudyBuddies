package com.example.studybuddies.di

import android.app.Application
import android.content.Context
import com.example.studybuddies.data.repository.AuthRepository
import com.example.studybuddies.data.repository.UserRepository
import com.example.studybuddies.viewmodel.AppViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Dependency Injection Container for the entire StudyBuddies application.
 * Responsible for providing instances of repositories and the ViewModel factory.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val viewModelFactory: AppViewModelFactory
}

// ZMIANA: Dodajemy 'context' do konstruktora, aby móc przekazać go do fabryki
class DefaultAppContainer(private val context: Context) : AppContainer {

    // 1. Initialization of Firebase services (Container-scoped Singletons)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Auth Repository.
     * We use the AuthRepository class, passing Auth and Firestore instances to it.
     * This allows for simultaneous account creation and profile creation in the database.
     */
    override val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    /**
     * User Repository.
     * Handles profile, lessons, tutor tiles, and messages.
     */
    override val userRepository: UserRepository by lazy {
        UserRepository(firestore)
    }

    /**
     * ViewModel Factory.
     * Passes ready-made repositories to all ViewModels in the application.
     * This guarantees that tutor tiles in Home and Search use the exact same database connection.
     */
    override val viewModelFactory: AppViewModelFactory by lazy {
        // NAPRAWA: Przekazujemy 'application' jako pierwszy argument,
        // ponieważ LessonsViewModel teraz tego wymaga do powiadomień.
        AppViewModelFactory(
            context.applicationContext as Application,
            authRepository,
            userRepository
        )
    }
}