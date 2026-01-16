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
 * Main Layout Screen handling the Bottom Navigation and Scaffold structure.
 * Updated: Optimized to ensure the passed ViewModel instances are used
 * correctly across all nested navigation destinations for real-time sync.
 */
@Composable
fun MainLayoutScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    lessonsViewModel: LessonsViewModel,
    chatViewModel: ChatViewModel,
    tutorProfileViewModel: TutorProfileViewModel,
    navController: NavHostController, // Parent controller
    onLogout: () -> Unit
) {
    // This controller manages the switching of screens within the Bottom Tabs
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val logoBlue = Color(0xFF1A73E8)
    val lightBlueUnselected = Color(0xFF90CAF9)

    // GLOBAL REFRESH: Ensures that data is fresh when entering the main authenticated area.
    // The use of LaunchedEffect(Unit) prevents unnecessary re-triggers.
    LaunchedEffect(Unit) {
        authViewModel.refreshUser()
        homeViewModel.refreshData()
        lessonsViewModel.loadLessons() // Ensuring lessons listener is active immediately
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            // Logic to hide the BottomBar on specific detail/edit screens
            if (currentRoute?.startsWith("chat_detail") != true &&
                currentRoute?.startsWith("tutor_profile") != true &&
                currentRoute != "edit_profile") {

                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    color = Color.White,
                    tonalElevation = 0.dp
                ) {
                    Column {
                        // Brand separator line
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFEEEEEE)))

                        NavigationBar(
                            modifier = Modifier.height(70.dp),
                            containerColor = Color.White,
                            tonalElevation = 0.dp
                        ) {
                            // Order: Chats, Lessons, Home, Search, Profile
                            val items = listOf(
                                Triple("chats", "Messages", Icons.Default.Chat),
                                Triple("lessons", "Lessons", Icons.Default.DateRange),
                                Triple("home", "Home", Icons.Default.Home),
                                Triple("search", "Search", Icons.Default.Search),
                                Triple("profile", "Profile", Icons.Default.Person)
                            )

                            items.forEach { (route, label, icon) ->
                                val selected = currentRoute == route

                                // PROFESSIONAL ICON LIFT ANIMATION
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
                                                .offset(y = iconYOffset)
                                        )
                                    },
                                    label = { },
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != route) {
                                            bottomNavController.navigate(route) {
                                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = logoBlue,
                                        unselectedIconColor = lightBlueUnselected,
                                        indicatorColor = Color.Transparent
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