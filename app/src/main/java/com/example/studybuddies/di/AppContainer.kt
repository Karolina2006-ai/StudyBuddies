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
 * This interface defines what the app needs to function.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val viewModelFactory: AppViewModelFactory
}

/**
 * The actual implementation of the container.
 * It's like a 'factory of factories' that creates everything in the right order.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    // 1. We create single instances of Firebase services here.
    // This ensures we aren't opening 50 different connections to the database.
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Auth Repository.
     * 'by lazy' means this won't be created until the very second it's actually needed.
     * This saves memory during app startup.
     */
    override val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    /**
     * User Repository.
     * We pass the shared 'firestore' instance so it's consistent with AuthRepository.
     */
    override val userRepository: UserRepository by lazy {
        UserRepository(firestore)
    }

    /**
     * ViewModel Factory.
     * This is the bridge between our data (Repositories) and our UI (ViewModels).
     */
    override val viewModelFactory: AppViewModelFactory by lazy {
        // We cast the context to 'Application' because some ViewModels (like Lessons)
        // need the application context to trigger system-level things like notifications.
        AppViewModelFactory(
            context.applicationContext as Application,
            authRepository,
            userRepository
        )
    }
}