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
 * Main Navigation Host for Study Buddies.
 * Updated: Ensures all destinations use the synchronized ViewModel instances
 * passed from MainActivity to guarantee real-time UI updates across the app.
 */
@Composable
fun StudyBuddiesNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    lessonsViewModel: LessonsViewModel,
    chatViewModel: ChatViewModel,
    tutorProfileViewModel: TutorProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // --- HOME SCREEN ---
        composable("home") {
            // TU BYŁ BŁĄD. Teraz przekazujemy lessonsViewModel!
            HomeScreen(
                homeViewModel = homeViewModel,
                lessonsViewModel = lessonsViewModel, // <--- TO JEST KLUCZOWE!
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
            // This screen now reacts instantly to any changes in lessonsViewModel
            LessonsScreen(viewModel = lessonsViewModel)
        }

        // --- CHAT LIST ---
        composable("chats") {
            ChatScreen(
                viewModel = chatViewModel,
                onChatClick = { chatId, chatName ->
                    navController.navigate("chat_detail/$chatId/$chatName")
                }
            )
        }

        // --- MY PROFILE ---
        composable("profile") {
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            val currentUser = authState.currentUser

            if (currentUser != null) {
                MyProfileScreen(
                    user = currentUser,
                    onEditClick = {
                        navController.navigate("edit_profile")
                    },
                    onLogout = onLogout,
                    lessonsViewModel = lessonsViewModel, // Sharing the same instance
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // --- TUTOR PUBLIC PROFILE ---
        composable(
            route = "tutor_profile/{tutorId}",
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId")
            if (tutorId != null) {
                // PROFESSIONAL SYNC:
                // We ensure that TutorPublicProfile uses the shared lessonsViewModel.
                // When a booking occurs here, it immediately triggers updates in Home and Lessons.
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