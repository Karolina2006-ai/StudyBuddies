package com.example.studybuddies

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.studybuddies.data.repository.AuthRepository
import com.example.studybuddies.data.repository.UserRepository
import com.example.studybuddies.ui.screens.MainLayoutScreen
import com.example.studybuddies.ui.screens.BlueWelcomeScreen
import com.example.studybuddies.ui.screens.auth.LoginScreen
import com.example.studybuddies.ui.screens.auth.RegistrationDetailsScreen
import com.example.studybuddies.ui.screens.auth.RegistrationRoleScreen
import com.example.studybuddies.ui.theme.StudyBuddiesTheme
import com.example.studybuddies.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Global disable of the Ripple effect for a clean, brand-consistent UI look.
 */
private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() { drawContent() }
        }
    }
    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- STEP: NOTIFICATION CHANNEL REGISTRATION ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "lessons_channel"
            val channelName = "Lesson Reminders"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = "Notifications for upcoming lessons"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Initialize Firebase instances and Repositories
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val authRepository = AuthRepository(auth, firestore)
        val userRepository = UserRepository(firestore)

        // ViewModel Factory for manual dependency injection across the app
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository, userRepository) as T
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(userRepository) as T
                    modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(userRepository) as T

                    // Pass 'application' context to LessonsViewModel for system services access
                    modelClass.isAssignableFrom(LessonsViewModel::class.java) -> LessonsViewModel(application, userRepository, auth) as T

                    modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(userRepository) as T
                    modelClass.isAssignableFrom(TutorProfileViewModel::class.java) -> TutorProfileViewModel(userRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        setContent {
            StudyBuddiesTheme {
                CompositionLocalProvider(LocalIndication provides NoIndication) {

                    // --- NOTIFICATION PERMISSION REQUEST LOGIC ---
                    val context = LocalContext.current

                    // Launcher to trigger the system "Allow notifications" dialog at startup
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* Choice is managed by the system */ }
                    )

                    // Check if Android 13+ (Tiramisu) and if permission is required
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                    val authState by authViewModel.authState.collectAsStateWithLifecycle()

                    // Internal app states: INITIAL -> AUTH -> WELCOME -> MAIN
                    var appState by remember { mutableStateOf("INITIAL") }

                    // --- GLOBAL NAVIGATION AND AUTH OBSERVER ---
                    LaunchedEffect(authState.isAuthenticated, authState.isLoading) {
                        if (!authState.isLoading) {
                            if (authState.isAuthenticated) {
                                // Transition to Welcome screen if coming from Auth or Initial
                                if (appState == "AUTH" || appState == "INITIAL") {
                                    appState = "WELCOME"
                                }
                            } else {
                                // Fallback to Auth if not authenticated
                                if (appState != "AUTH") {
                                    appState = "AUTH"
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = appState,
                        label = "AppNavTransition",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                        }
                    ) { state ->
                        when (state) {
                            "INITIAL" -> Box(Modifier.fillMaxSize().background(Color.White))

                            "WELCOME" -> BlueWelcomeScreen(onAnimationFinished = {
                                // Ensure user profile is fully loaded before entering MAIN layout
                                if (authState.currentUser != null) {
                                    appState = "MAIN"
                                } else {
                                    authViewModel.refreshUser()
                                }
                            })

                            "AUTH" -> {
                                val navController = rememberNavController()
                                NavHost(navController, startDestination = "login") {
                                    composable("login") {
                                        LoginScreen(
                                            authViewModel = authViewModel,
                                            onNavigateToRegister = { navController.navigate("role") },
                                            onLoginSuccess = { }
                                        )
                                    }

                                    composable("role") {
                                        RegistrationRoleScreen(
                                            onNavigateToDetails = { role ->
                                                navController.navigate("details/$role")
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }

                                    composable("details/{role}") { backStackEntry ->
                                        val roleArg = backStackEntry.arguments?.getString("role") ?: "Student"
                                        RegistrationDetailsScreen(
                                            role = roleArg,
                                            viewModel = authViewModel,
                                            onNavigateToLogin = { navController.navigate("login") },
                                            onRegistrationSuccess = { },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }

                            "MAIN" -> {
                                if (authState.currentUser != null) {
                                    MainLayoutScreen(
                                        authViewModel = authViewModel,
                                        homeViewModel = viewModel(
                                            key = "home_shared",
                                            factory = viewModelFactory
                                        ),
                                        searchViewModel = viewModel(
                                            key = "search_shared",
                                            factory = viewModelFactory
                                        ),
                                        lessonsViewModel = viewModel(
                                            key = "lessons_shared", // SINGLETON KEY
                                            factory = viewModelFactory
                                        ),
                                        chatViewModel = viewModel(
                                            key = "chat_shared",
                                            factory = viewModelFactory
                                        ),
                                        tutorProfileViewModel = viewModel(
                                            key = "tutor_prof_shared",
                                            factory = viewModelFactory
                                        ),
                                        navController = rememberNavController(),
                                        onLogout = {
                                            authViewModel.logout()
                                        }
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize().background(Color.White))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}