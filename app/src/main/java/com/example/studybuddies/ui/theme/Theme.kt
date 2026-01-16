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

// --- Material 3 Color Scheme Mapping ---

private val LightColorScheme = lightColorScheme(
    primary = StudyBuddiesPrimary,       // Button background (Blue)
    onPrimary = Color.White,             // <--- FIX: White text on buttons

    primaryContainer = StudyBuddiesLightBlue,
    onPrimaryContainer = StudyBuddiesPrimary,

    secondary = StudyBuddiesAccent,
    onSecondary = Color.White,

    // App Backgrounds - forced White
    background = Color.White,
    onBackground = Color.Black,          // Text on screens (Black)

    surface = Color.White,
    onSurface = Color.Black,             // Text on cards (Black)

    outline = StudyBuddiesOutline,
    surfaceVariant = StudyBuddiesLightBlue,
    onSurfaceVariant = Color.Black,

    error = Color(0xFFB00020),
    onError = Color.White
)

// Dark Mode - preserved from your file, but currently unused
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
    darkTheme: Boolean = false, // Parameter ignored to enforce Light Mode
    content: @Composable () -> Unit
) {
    // Select scheme (Logic forces LightColorScheme for consistency)
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // White status bar
            window.statusBarColor = Color.White.toArgb()
            // Dark icons on the status bar (so they are visible on white)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // References Type.kt
        content = content
    )
}