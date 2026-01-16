package com.example.studybuddies.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.User
import com.example.studybuddies.viewmodel.LessonsViewModel
import com.example.studybuddies.viewmodel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    user: User,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    lessonsViewModel: LessonsViewModel,
    authViewModel: AuthViewModel? = null
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlueBg = Color(0xFFF0F5FF)
    val dividerColor = Color(0xFFF7F7F7)

    var showRateDialog by remember { mutableStateOf(false) }

    // FIX 1: Using remember with user.hourlyRate key.
    var currentRate by remember(user.hourlyRate) { mutableStateOf(user.hourlyRate.toString()) }

    // Logic availability
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullDaysMap = mapOf(
        "Mon" to "Monday", "Tue" to "Tuesday", "Wed" to "Wednesday",
        "Thu" to "Thursday", "Fri" to "Friday", "Sat" to "Saturday", "Sun" to "Sunday"
    )
    val timeSlots = listOf(
        "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM",
        "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM"
    )

    var selectedDayShort by remember { mutableStateOf("Mon") }

    // FIX 2: Guarantees that the availability view is always in sync with user data.
    var availabilityState by remember(user.availability) { mutableStateOf(user.availability) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            // Logout Icon (No Ripple)
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = logoBlue,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onLogout() }
            )
        }

        // --- AVATAR & DATA ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(lightBlueBg),
                contentAlignment = Alignment.Center
            ) {
                if (!user.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(user.initials, color = logoBlue, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = user.fullName,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 16.dp),
                color = Color.Black
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.School, null, modifier = Modifier.size(16.dp), tint = logoBlue)
                Text(user.university, color = Color.Gray, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = logoBlue)
                Text(user.city, color = Color.Gray, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
            }

            // Hourly Rate (Tutor Only) with Edit
            if (user.isTutor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = "${user.hourlyRate.toInt()} PLN / hour",
                        color = logoBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Rate",
                        tint = logoBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showRateDialog = true }
                    )
                }
            }

            // --- FIXED BUTTON ---
            Button(
                onClick = { onEditClick() }, // Triggers navigation action
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = logoBlue)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile Data", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        HorizontalDivider(thickness = 8.dp, color = dividerColor, modifier = Modifier.padding(vertical = 24.dp))

        // --- SUBJECTS (Everyone) ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = user.roleSectionTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.subjects.forEach { subject ->
                    Surface(
                        color = lightBlueBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = subject,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = logoBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- BIO ---
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Bio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(
                text = user.bio.ifEmpty { "No bio available." },
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp
            )
        }

        // --- HOBBIES (Everyone) ---
        if (user.hobbies.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Hobbies & Interests", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                }
                FlowRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    user.hobbies.forEach { hobby ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.5f)),
                            color = Color.White
                        ) {
                            Text(hobby, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp, color = logoBlue)
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 8.dp, color = dividerColor, modifier = Modifier.padding(vertical = 24.dp))
        }

        // --- AVAILABILITY (Tutor Only) ---
        if (user.isTutor) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("My Availability", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = logoBlue)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { day ->
                        val isSelected = selectedDayShort == day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) logoBlue else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedDayShort = day }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = day,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                val fullDayName = fullDaysMap[selectedDayShort] ?: "Monday"
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeSlots.forEach { time ->
                        val isSelected = availabilityState[fullDayName]?.contains(time) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val currentList = availabilityState[fullDayName]?.toMutableList() ?: mutableListOf()
                                if (isSelected) currentList.remove(time) else currentList.add(time)
                                // FIX: Wrap in HashMap to match strict type requirement of User model
                                availabilityState = HashMap(availabilityState + (fullDayName to currentList))
                            },
                            label = { Text(time) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = logoBlue, selectedLabelColor = Color.White, labelColor = logoBlue),
                            border = FilterChipDefaults.filterChipBorder(borderColor = logoBlue, enabled = true, selected = isSelected)
                        )
                    }
                }

                Button(
                    onClick = {
                        lessonsViewModel.saveAvailability(availabilityState)
                        authViewModel?.refreshUser()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Availability Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(thickness = 8.dp, color = dividerColor, modifier = Modifier.padding(vertical = 24.dp))
        }

        // --- DASHBOARD (Tutor Only) ---
        if (user.isTutor) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Tutor Dashboard", color = logoBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                Text("My Rating", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

                Surface(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    color = Color(0xFFFFF9E6),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Average Score", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
                            Text("Based on all reviews", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
                            Text(
                                text = String.format(" %.1f", user.averageRating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                listOf("5", "4", "3", "2", "1").forEach { star ->
                    val count = user.ratingStats[star]?.toInt() ?: 0
                    val total = user.totalReviews.takeIf { it > 0 } ?: 1
                    val percentage = ((count.toFloat() / total) * 100).toInt()
                    RatingProgressBar(star.toInt(), percentage)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Received Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                if (user.reviews.isEmpty()) {
                    Text("No reviews yet.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                } else {
                    user.reviews.forEach { review ->
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(review.userName, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(review.comment, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                            HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color(0xFFF1F1F1))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Update Rate") },
            text = {
                OutlinedTextField(
                    value = currentRate,
                    onValueChange = { currentRate = it },
                    label = { Text("PLN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = logoBlue,
                        focusedTextColor = Color.Black
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentRate.toDoubleOrNull()?.let {
                            lessonsViewModel.updateHourlyRate(it)
                            authViewModel?.refreshUser()
                        }
                        showRateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = logoBlue)
                ) {
                    // FORCED WHITE COLOR
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showRateDialog = false }) { Text("Cancel", color = Color.Black) } },
            containerColor = Color.White
        )
    }
}

@Composable
fun RatingProgressBar(stars: Int, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$stars", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp), color = Color.Black)
        Icon(Icons.Default.Star, null, Modifier.size(14.dp), Color(0xFFFFC107))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp).height(8.dp).clip(CircleShape),
            color = Color(0xFFFFC107),
            trackColor = Color(0xFFE0E0E0),
        )
        Text("$percentage%", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium, modifier = Modifier.width(35.dp))
    }
}