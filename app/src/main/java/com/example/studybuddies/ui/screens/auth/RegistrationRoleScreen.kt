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

@Composable
fun RegistrationRoleScreen(
    onNavigateToDetails: (String) -> Unit,
    onBack: () -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Main App Logo
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )

        Text(
            text = "Join Study Buddies",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = logoBlue,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "Choose your primary role",
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(32.dp))

        // STUDENT CARD
        SimpleRoleCard(
            title = "Student",
            description = "I want to learn from tutors",
            iconRes = R.drawable.ic_student_graphic,
            logoBlue = logoBlue,
            onClick = { onNavigateToDetails("Student") }
        )

        Spacer(Modifier.height(20.dp))

        // TUTOR CARD
        SimpleRoleCard(
            title = "Tutor",
            description = "I want to help students learn",
            iconRes = R.drawable.ic_tutor_graphic,
            logoBlue = logoBlue,
            onClick = { onNavigateToDetails("Tutor") }
        )

        Spacer(Modifier.weight(1f))

        // "Already have an account? Login" link (Ripple disabled)
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
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() }
                .padding(bottom = 24.dp)
        )
    }
}

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
            .height(185.dp) // Height adapted to the 86.dp icon
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, logoBlue.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // SIZE: 86.dp (reduced by 10% from 96.dp as requested)
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(86.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = logoBlue
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}