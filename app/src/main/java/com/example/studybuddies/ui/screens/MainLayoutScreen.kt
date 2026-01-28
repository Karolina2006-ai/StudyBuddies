package com.example.studybuddies.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studybuddies.ui.navigation.StudyBuddiesNavHost
import com.example.studybuddies.viewmodel.*

/**
 * Main Layout Screen handling the Bottom Navigation and Scaffold structure
 * Ensured that the passed ViewModel instances are used
 * correctly across all nested navigation destinations for real-time sync
 */
@Composable
fun MainLayoutScreen(
    authViewModel: AuthViewModel, // Managing user login state and session data
    homeViewModel: HomeViewModel, // Handling the dashboard and personalized content
    searchViewModel: SearchViewModel, // Logic for finding tutors and filtering results
    lessonsViewModel: LessonsViewModel, // Controlling the scheduling and booking flow
    chatViewModel: ChatViewModel, // Managing real-time messaging and chat history
    tutorProfileViewModel: TutorProfileViewModel, // Handling details for the tutor view
    navController: NavHostController, // The top-level controller for global app navigation
    onLogout: () -> Unit // Action to trigger when the student decides to log out
) {
    // This controller manages the switching of screens within the Bottom Tabs
    val bottomNavController = rememberNavController() // Internal controller for the bottom menu
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState() // Tracking the current screen
    val currentRoute = navBackStackEntry?.destination?.route // Getting the name of the current route

    val logoBlue = Color(0xFF1A73E8) // Professional blue used for active selections
    val lightBlueUnselected = Color(0xFF90CAF9) // Lighter blue for inactive icons

    // GLOBAL REFRESH: Ensures that data is fresh when entering the main authenticated area.
    // The use of LaunchedEffect(Unit) prevents unnecessary re-triggers.
    LaunchedEffect(Unit) {
        authViewModel.refreshUser() // Update local user info from Firebase
        homeViewModel.refreshData() // Pull the latest dashboard updates
        lessonsViewModel.loadLessons() // Start the listener for scheduled sessions
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White, // Clean white background for the whole app
        bottomBar = {
            // Logic to hide the BottomBar on specific detail/edit screens for better focus
            if (currentRoute?.startsWith("chat_detail") != true &&
                currentRoute?.startsWith("tutor_profile") != true &&
                currentRoute != "edit_profile") {

                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(), // Ensures it stays above system buttons
                    color = Color.White,
                    tonalElevation = 0.dp
                ) {
                    Column {
                        // Brand separator line to visually divide content from navigation
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFEEEEEE)))

                        NavigationBar(
                            modifier = Modifier.height(70.dp),
                            containerColor = Color.White,
                            tonalElevation = 0.dp
                        ) {
                            // Order of navigation: Chats, Lessons, Home, Search, Profile
                            val items = listOf(
                                Triple("chats", "Messages", Icons.Default.Chat),
                                Triple("lessons", "Lessons", Icons.Default.DateRange),
                                Triple("home", "Home", Icons.Default.Home),
                                Triple("search", "Search", Icons.Default.Search),
                                Triple("profile", "Profile", Icons.Default.Person)
                            )

                            items.forEach { (route, label, icon) ->
                                val selected = currentRoute == route // Checking if this tab is active

                                // PROFESSIONAL ICON LIFT ANIMATION: Moves the icon up slightly when tapped
                                val iconYOffset by animateDpAsState(
                                    targetValue = if (selected) (-6).dp else 0.dp,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "iconLiftAnimation"
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .offset(y = iconYOffset) // Applying the lift effect
                                        )
                                    },
                                    label = { }, // Keeping UI clean by not showing text labels
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != route) {
                                            bottomNavController.navigate(route) {
                                                // Avoid building up a large stack of destinations
                                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                    saveState = true // Keep screen data when switching away
                                                }
                                                launchSingleTop = true // Don't open the same screen twice
                                                restoreState = true // Return to the same scroll position
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = logoBlue, // Blue for active tab
                                        unselectedIconColor = lightBlueUnselected, // Gray-blue for idle tabs
                                        indicatorColor = Color.Transparent // Removing the default background oval
                                    ),
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // COMPONENT SYNC: Passing the actual ViewModel instances into the NavHost
        // ensuring all screens share the same data source (Single Source of Truth).
        Box(modifier = Modifier.padding(innerPadding)) {
            StudyBuddiesNavHost(
                navController = bottomNavController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                searchViewModel = searchViewModel,
                lessonsViewModel = lessonsViewModel,
                chatViewModel = chatViewModel,
                tutorProfileViewModel = tutorProfileViewModel,
                onLogout = onLogout
            )
        }
    }
}