package com.example.studybuddies.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studybuddies.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Professional Welcome Screen.
 * Updated: Text split into two lines, centered, with correct line height.
 */
@Composable
fun BlueWelcomeScreen(
    onAnimationFinished: () -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)

    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    var displayedText by remember { mutableStateOf("") }

    // CHANGE: Newline character "\n" splits the text
    val fullText = "Studying made\nsimple!"

    LaunchedEffect(Unit) {
        // Run logo animations in parallel
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200)
            )
        }

        // Wait for logo to settle, then type the text
        delay(800)
        fullText.forEachIndexed { index, _ ->
            displayedText = fullText.substring(0, index + 1)
            delay(50) // Typing speed
        }

        // Hold the screen for a moment before navigating away
        delay(1500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(logoBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.study_buddy_mascot),
                contentDescription = "Study Buddy Mascot",
                modifier = Modifier
                    .size(280.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CHANGE: Professional text styling
            Text(
                text = displayedText,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, // Centers both lines of text
                lineHeight = 44.sp // Line height - prevents letters from overlapping
            )
        }
    }
}