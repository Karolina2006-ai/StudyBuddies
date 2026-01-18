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
 * Using an enum is a clean way to avoid typos that happen with 'String' routes.
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
 * This makes the code modular: if we want to add a tab, we just add it to the list.
 */
data class BottomNavItem(
    val title: String,         // Text shown under the icon
    val route: HomeTab,        // The enum value used for navigation logic
    val icon: ImageVector,     // The "empty" outline icon (not selected)
    val selectedIcon: ImageVector // The "filled" icon (when active)
)

/**
 * Configuration for the bottom navigation bar.
 * This list is looped through in the MainLayoutScreen to draw the UI.
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