package com.example.studybuddies

import android.app.Application
import com.example.studybuddies.di.AppContainer
import com.example.studybuddies.di.DefaultAppContainer

/**
 * StudyBuddiesApplication - The main application class.
 * Responsible for initializing the Dependency Injection (DI) container (Rule 6).
 */
class StudyBuddiesApplication : Application() {

    /**
     * Container storing repository instances and ViewModel factories (Rule 7).
     * This acts as a central hub for all app dependencies.
     */
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        /**
         * Initialization of the dependency container.
         * DefaultAppContainer provides Firebase and Repositories to the rest of the app.
         * NAPRAWA: Przekazujemy 'this' (kontekst aplikacji), bo jest potrzebny do powiadomień.
         */
        appContainer = DefaultAppContainer(this)
    }
}