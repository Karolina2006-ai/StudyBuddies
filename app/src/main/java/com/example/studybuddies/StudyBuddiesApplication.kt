package com.example.studybuddies

import android.app.Application
import com.example.studybuddies.di.AppContainer
import com.example.studybuddies.di.DefaultAppContainer

/**
 * StudyBuddiesApplication - The main application entry point.
 * Responsible for initializing the Dependency Injection (DI) container.
 */
class StudyBuddiesApplication : Application() {

    /**
     * Container storing repository instances and ViewModel factories.
     * This acts as a central hub for all application dependencies, ensuring
     * that data sources are consistent across the entire app.
     */
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        /**
         * Initialization of the dependency container.
         * DefaultAppContainer provides Firebase instances and Repositories.
         * FIX: We pass 'this' (the application context) because it is required
         * for system-level services like notification scheduling.
         */
        appContainer = DefaultAppContainer(this)
    }
}