package com.example.studybuddies.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color


val Typography = Typography(
    // Screen Headers - Used for main titles at the top of each screen
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, // Standard system font for high compatibility
        fontWeight = FontWeight.Bold,    // Strong weight to emphasize screen purpose
        fontSize = 22.sp,                // Large enough to act as a primary anchor
        lineHeight = 28.sp,              // Extra vertical space for multi-line headers
        letterSpacing = 0.sp,            // Minimal spacing for tight, professional headers
        color = Color.Black              // High contrast against white backgrounds
    ),

    // Section Titles - Used for sub-headings like "Recommended Tutors"
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,    // Clear distinction from body text
        fontSize = 18.sp,                // Sized to separate different UI sections
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = Color.Black
    ),

    // Standard Body Text - Used for general descriptions and tutor bios
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,  // Light weight for maximum readability
        fontSize = 16.sp,                // Standard size for comfortable mobile reading
        lineHeight = 24.sp,              // Generous line height to prevent eye fatigue
        letterSpacing = 0.5.sp,          // Increased tracking to improve legibility
        color = Color.Black
    ),

    // Smaller Body Text - Used for secondary details like "PLN/h" or dates
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,                // Smaller size for less critical metadata
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = Color.Black
    ),

    // Button Text (Login, Create Account) - Styled for clear calls to action
    // FIX: Using Unspecified allows the onPrimary color (White) from Theme.kt to take precedence
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,    // Bold to make actions stand out
        fontSize = 14.sp,                // Standard button text size
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = Color.Unspecified        // Inherits color from the parent component (e.g., White text on Blue)
    )
)