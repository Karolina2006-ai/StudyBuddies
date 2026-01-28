package com.example.studybuddies.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.ui.screens.home.HomeScreen
import com.example.studybuddies.ui.screens.search.SearchScreen
import com.example.studybuddies.ui.screens.lessons.LessonsScreen
import com.example.studybuddies.ui.screens.chat.ChatScreen
import com.example.studybuddies.ui.screens.chat.ChatDetailScreen
import com.example.studybuddies.ui.screens.profile.MyProfileScreen
import com.example.studybuddies.ui.screens.profile.TutorPublicProfile
import com.example.studybuddies.ui.screens.profile.EditProfileScreen
import com.example.studybuddies.viewmodel.*

/**
 * Main Navigation Host for Study Buddies
 * Ensures all destinations use the synchronized ViewModel instances
 * passed from MainActivity to ensure real-time UI updates across the app
 */
@Composable
fun StudyBuddiesNavHost(
    navController: NavHostController, // Standard controller to switch between screens
    authViewModel: AuthViewModel,     // ViewModels are passed as parameters to share state
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    lessonsViewModel: LessonsViewModel,
    chatViewModel: ChatViewModel,
    tutorProfileViewModel: TutorProfileViewModel,
    onLogout: () -> Unit
) {
    // NavHost connects the navController to a navigation graph
    NavHost(
        navController = navController,
        startDestination = "home" // Defines the first screen the user sees
    ) {
        // --- HOME SCREEN ---
        composable("home") {
            // Passing the shared lessonsViewModel ensures home screen lists stay updated
            HomeScreen(
                homeViewModel = homeViewModel,
                lessonsViewModel = lessonsViewModel,
                onTutorClick = { tutorId ->
                    navController.navigate("tutor_profile/$tutorId")
                }
            )
        }

        // --- SEARCH SCREEN ---
        composable("search") {
            SearchScreen(
                viewModel = searchViewModel,
                onTutorClick = { tutorId ->
                    navController.navigate("tutor_profile/$tutorId")
                }
            )
        }

        // --- LESSONS SCREEN ---
        composable("lessons") {
            // Reacts instantly to changes in lessons (like a new booking or cancellation)
            LessonsScreen(viewModel = lessonsViewModel)
        }

        // --- CHAT LIST ---
        composable("chats") {
            ChatScreen(
                viewModel = chatViewModel,
                onChatClick = { chatId, chatName ->
                    // Navigate to conversation using unique ID and name as arguments
                    navController.navigate("chat_detail/$chatId/$chatName")
                }
            )
        }

        // --- MY PROFILE ---
        composable("profile") {
            // collectAsStateWithLifecycle is used for lifecycle-aware state observation
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            val currentUser = authState.currentUser

            if (currentUser != null) {
                MyProfileScreen(
                    user = currentUser,
                    onEditClick = {
                        navController.navigate("edit_profile")
                    },
                    onLogout = onLogout,
                    lessonsViewModel = lessonsViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        // --- EDIT PROFILE SCREEN ---
        composable("edit_profile") {
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            val currentUser = authState.currentUser

            if (currentUser != null) {
                EditProfileScreen(
                    user = currentUser,
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() } // Standard back button logic
                )
            }
        }

        // --- TUTOR PUBLIC PROFILE ---
        // Route includes a variable placeholder {tutorId}
        composable(
            route = "tutor_profile/{tutorId}",
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extracting the tutorId from the navigation path
            val tutorId = backStackEntry.arguments?.getString("tutorId")
            if (tutorId != null) {
                // Syncing shared lessonsViewModel here allows bookings made on this screen
                // to appear immediately in the "Lessons" and "Home" tabs.
                TutorPublicProfile(
                    tutorId = tutorId,
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    lessonsViewModel = lessonsViewModel,
                    tutorProfileViewModel = tutorProfileViewModel,
                    onChatClick = { chatId, chatName ->
                        navController.navigate("chat_detail/$chatId/$chatName")
                    }
                )
            }
        }

        // --- CHAT DETAIL ---
        // Passing specific ID for Firestore query and name for the TopBar title
        composable(
            route = "chat_detail/{chatId}/{chatName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: "Chat"

            ChatDetailScreen(
                chatId = chatId,
                chatName = chatName,
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}