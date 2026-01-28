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

/**
 * Main Profile Screen: Displays user info, tutor dashboard, and availability
 * Student perspective: I can see my details and navigate to edit them
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    user: User, // The current user object containing all profile data
    onEditClick: () -> Unit, // Callback to navigate to the Edit Screen
    onLogout: () -> Unit, // Callback to sign out
    lessonsViewModel: LessonsViewModel, // Handles hourly rate and availability updates
    authViewModel: AuthViewModel? = null // Optional VM to trigger data refreshes
) {
    val logoBlue = Color(0xFF1A73E8) // Our signature blue color
    val lightBlueBg = Color(0xFFF0F5FF) // Used for chips and avatar backgrounds
    val dividerColor = Color(0xFFF7F7F7) // Light gray for visual sectioning

    // State for the popup that changes the tutor's price
    var showRateDialog by remember { mutableStateOf(false) }

    // Ensures the local price state updates if the user object changes from the backend
    var currentRate by remember(user.hourlyRate) { mutableStateOf(user.hourlyRate.toString()) }

    // Hardcoded lists for the calendar-style availability view
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

    // This local state tracks the user's availability schedule before saving
    var availabilityState by remember(user.availability) { mutableStateOf(user.availability) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // Allows scrolling through the full profile
    ) {
        // --- HEADER SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            // Logout Button: Triggers the sign-out logic
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = logoBlue,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Clean design, no ripple
                    ) { onLogout() }
            )
        }

        // --- AVATAR & BASIC DATA ---
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
                // Shows the profile pic if it exists, otherwise defaults to initials
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

            // University and City sub-labels
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.School, null, modifier = Modifier.size(16.dp), tint = logoBlue)
                Text(user.university, color = Color.Gray, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = logoBlue)
                Text(user.city, color = Color.Gray, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
            }

            // Price Section: Only tutors see this
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
                    // Opens the "Update Rate" dialog
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

            // Main navigation button to the Edit screen
            Button(
                onClick = { onEditClick() },
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

        // Section separator
        HorizontalDivider(thickness = 8.dp, color = dividerColor, modifier = Modifier.padding(vertical = 24.dp))

        // --- SUBJECTS SECTION ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = user.roleSectionTitle, // Usually "Subjects I Teach" or "Subjects I Study"
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            // Displays tags that wrap to the next line automatically
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

        // --- BIO SECTION ---
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Bio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(
                text = user.bio.ifEmpty { "No bio available." },
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp
            )
        }

        // --- HOBBIES SECTION ---
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

        // --- TUTOR AVAILABILITY MANAGEMENT ---
        if (user.isTutor) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("My Availability", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = logoBlue)

                // Day selector row
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

                // Grid of time slots for the selected day
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
                                // Update local availability state before saving to DB
                                availabilityState = HashMap(availabilityState + (fullDayName to currentList))
                            },
                            label = { Text(time) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = logoBlue, selectedLabelColor = Color.White, labelColor = logoBlue),
                            border = FilterChipDefaults.filterChipBorder(borderColor = logoBlue, enabled = true, selected = isSelected)
                        )
                    }
                }

                // Save button to persist availability changes to Firestore
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

        // --- TUTOR DASHBOARD (STATS & REVIEWS) ---
        if (user.isTutor) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Tutor Dashboard", color = logoBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                Text("My Rating", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

                // Summary Score Card
                Surface(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    color = Color(0xFFFFF9E6), // Light gold background
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
                // Visual breakdown of star ratings (5 down to 1)
                listOf("5", "4", "3", "2", "1").forEach { star ->
                    val count = user.ratingStats[star]?.toInt() ?: 0
                    val total = user.totalReviews.takeIf { it > 0 } ?: 1
                    val percentage = ((count.toFloat() / total) * 100).toInt()
                    RatingProgressBar(star.toInt(), percentage)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Received Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                // List of textual feedback from students
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

    // --- DIALOG FOR UPDATING TUTOR HOURLY RATE ---
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
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showRateDialog = false }) { Text("Cancel", color = Color.Black) } },
            containerColor = Color.White
        )
    }
}

/**
 * Custom progress bar for the rating breakdown section
 */
@Composable
fun RatingProgressBar(stars: Int, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$stars", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp), color = Color.Black)
        Icon(Icons.Default.Star, null, Modifier.size(14.dp), Color(0xFFFFC107))
        // Shows the relative percentage of reviews with this star count
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp).height(8.dp).clip(CircleShape),
            color = Color(0xFFFFC107),
            trackColor = Color(0xFFE0E0E0),
        )
        Text("$percentage%", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium, modifier = Modifier.width(35.dp))
    }
}