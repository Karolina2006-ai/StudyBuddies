package com.example.studybuddies.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.studybuddies.R

/**
 * Screen that allows the user to choose between being a Student or a Tutor
 * This choice determines the profile layout and available features later in the app
 */
@Composable
fun RegistrationRoleScreen(
    onNavigateToDetails: (String) -> Unit, // Callback to pass the chosen role to the next screen
    onBack: () -> Unit // Callback to return to the Login screen
) {
    // Brand color definition
    val logoBlue = Color(0xFF1A73E8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        Spacer(Modifier.height(40.dp))

        // Application Logo
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null, // Decorative image
            modifier = Modifier.size(100.dp)
        )

        // Main Header
        Text(
            text = "Join Study Buddies",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = logoBlue,
            modifier = Modifier.padding(top = 24.dp)
        )

        // Sub-header instructions
        Text(
            text = "Choose your primary role",
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(32.dp))

        // SELECTION OPTION: STUDENT
        SimpleRoleCard(
            title = "Student",
            description = "I want to learn from tutors",
            iconRes = R.drawable.ic_student_graphic,
            logoBlue = logoBlue,
            onClick = { onNavigateToDetails("Student") } // Pass role string to the next stage
        )

        Spacer(Modifier.height(20.dp))

        // SELECTION OPTION: TUTOR
        SimpleRoleCard(
            title = "Tutor",
            description = "I want to help students learn",
            iconRes = R.drawable.ic_tutor_graphic,
            logoBlue = logoBlue,
            onClick = { onNavigateToDetails("Tutor") } // Pass role string to the next stage
        )

        // Push the footer to the bottom of the screen
        Spacer(Modifier.weight(1f))

        // Link to navigate back to login
        Text(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(color = logoBlue, fontWeight = FontWeight.Bold)) {
                    append("Login")
                }
            },
            color = Color.Black,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable(
                    // interactionSource + indication = null removes the gray 'ripple' box on click
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() }
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * Custom Card component representing a role choice
 * Encapsulates design logic for the role selection buttons
 */
@Composable
fun SimpleRoleCard(
    title: String,
    description: String,
    iconRes: Int,
    logoBlue: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp) // Specific height to accommodate the icon size
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, logoBlue.copy(alpha = 0.8f)) // Subtly transparent border
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual representation of the role
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(86.dp) // Carefully sized to fit the card layout
            )

            Spacer(Modifier.height(12.dp))

            // Role Title
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = logoBlue
            )

            // Role Explanation
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}