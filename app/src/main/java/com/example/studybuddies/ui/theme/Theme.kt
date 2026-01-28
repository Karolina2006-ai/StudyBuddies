package com.example.studybuddies.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val LightColorScheme = lightColorScheme(
    primary = StudyBuddiesPrimary,       // The main brand blue used for buttons and key actions
    onPrimary = Color.White,             // Ensures text on top of blue buttons is white and readable

    primaryContainer = StudyBuddiesLightBlue, // Used for highlighted background areas
    onPrimaryContainer = StudyBuddiesPrimary, // Text color used inside primary containers

    secondary = StudyBuddiesAccent,      // Accent green for status and secondary highlights
    onSecondary = Color.White,           // Text color used on top of green elements

    // App Backgrounds - specifically forced to White for a clean, modern look
    background = Color.White,
    onBackground = Color.Black,          // Primary text color for screen content

    surface = Color.White,               // Background color for cards and menus
    onSurface = Color.Black,             // Text color for content placed on cards

    outline = StudyBuddiesOutline,       // Color used for borders and thin divider lines
    surfaceVariant = StudyBuddiesLightBlue, // Alternative surface color for subtle contrast
    onSurfaceVariant = Color.Black,      // Content color for surface variants

    error = Color(0xFFB00020),           // Standard red for error messages and alerts
    onError = Color.White                // White text for high visibility on red backgrounds
)

// Dark Mode configuration - preserved for future implementation but currently inactive
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White
)

@Composable
fun StudyBuddiesTheme(
    darkTheme: Boolean = false, // This parameter is currently ignored to enforce a Light Theme
    content: @Composable () -> Unit // The UI content that will be styled by this theme
) {
    // Selection logic - currently hardcoded to Light for brand consistency
    val colorScheme = LightColorScheme
    val view = LocalView.current // Accessing the current view to modify system windows

    if (!view.isInEditMode) {
        // Runs whenever the theme is applied to update the system status bar
        SideEffect {
            val window = (view.context as Activity).window
            // Set the system status bar color to White to match the app header
            window.statusBarColor = Color.White.toArgb()
            // Configure status bar icons to be Dark so they are visible on the white background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    // Applying the calculated colors and typography to the entire application
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // References the custom fonts defined in Type.kt
        content = content
    )
}