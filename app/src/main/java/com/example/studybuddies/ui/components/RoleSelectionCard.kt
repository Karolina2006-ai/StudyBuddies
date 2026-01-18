package com.example.studybuddies.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Role Selection Card.
 * Component used for selecting a role (Student/Tutor) during registration.
 */
@Composable
fun RoleSelectionCard(
    title: String,      // "I'm a Student" or "I'm a Tutor"
    description: String,// Small text explaining what the role does
    iconRes: Int,       // Resource ID for the drawable image (icon)
    isSelected: Boolean,// State passed from the ViewModel/Screen to show if it's picked
    onClick: () -> Unit // Lambda function to handle the click event
) {
    // Custom colors to match the app's brand identity
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable(
                // We use 'remember' so the interaction source isn't recreated on every redraw.
                interactionSource = remember { MutableInteractionSource() },
                // Setting indication to 'null' removes the standard gray 'ripple' effect.
                // This makes the UI feel cleaner and more professional.
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        // The border gets thicker and turns blue when the card is selected.
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = logoBlue
        ),
        // If selected, the background turns a soft light blue.
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) lightBlueBg else Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp) // Flat design style
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Displays the student/tutor illustration.
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null, // Decorative image, so null description is fine.
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The main Label (Role Name).
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = logoBlue,
                fontSize = 22.sp
            )

            // The helper description in standard black text.
            Text(
                text = description,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}