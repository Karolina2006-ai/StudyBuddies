package com.example.studybuddies.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation tabs for the bottom bar.
 * Tab definition. The logic for removing the ripple effect is handled in MainLayoutScreen.
 */
enum class HomeTab {
    CHAT,
    LESSONS,
    HOME,
    SEARCH,
    PROFILE
}

/**
 * Data class representing a single item in the bottom navigation bar.
 */
data class BottomNavItem(
    val title: String,
    val route: HomeTab,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/**
 * Configuration for the bottom navigation bar.
 */
val BottomNavItems = listOf(
    BottomNavItem(
        title = "Chat",
        route = HomeTab.CHAT,
        icon = Icons.Outlined.Chat,
        selectedIcon = Icons.Filled.Chat
    ),
    BottomNavItem(
        title = "Lessons",
        route = HomeTab.LESSONS,
        icon = Icons.Outlined.DateRange,
        selectedIcon = Icons.Filled.DateRange
    ),
    BottomNavItem(
        title = "Home",
        route = HomeTab.HOME,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    ),
    BottomNavItem(
        title = "Search",
        route = HomeTab.SEARCH,
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search
    ),
    BottomNavItem(
        title = "Profile",
        route = HomeTab.PROFILE,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
)