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
 * Follows design rules: No ripple effect, black descriptions, blue borders.
 */
@Composable
fun RoleSelectionCard(
    title: String,
    description: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable(
                // Rule 2: Complete removal of the gray ripple effect on click
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        // Blue border when selected
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = logoBlue
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) lightBlueBg else Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Role Icon
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title (e.g., "I'm a Student") - Blue
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = logoBlue,
                fontSize = 22.sp
            )

            // Description - Black (Rule 1)
            Text(
                text = description,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}