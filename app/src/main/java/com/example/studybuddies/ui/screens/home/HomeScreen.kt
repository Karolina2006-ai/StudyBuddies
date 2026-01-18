package com.example.studybuddies.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.User
import com.example.studybuddies.viewmodel.HomeViewModel
import com.example.studybuddies.viewmodel.LessonsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home Screen displaying personalized greetings, upcoming lessons, and recommendations.
 * FIX: Now using LessonsViewModel directly for lessons to ensure instant updates.
 * SORTING UPDATE: Lessons are now sorted chronologically (Nearest date first).
 */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel, // This VM handles my user info and the recommendations
    lessonsViewModel: LessonsViewModel, // This VM is just for my calendar/lessons data
    onTutorClick: (String) -> Unit // What happens when I tap a tutor's profile
) {
    // Getting the latest UI state for the user's name and recommended list
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Listening to the lessons list so it updates automatically if one gets booked or cancelled
    val allLessons by lessonsViewModel.lessons.collectAsStateWithLifecycle()

    val logoBlue = Color(0xFF1A73E8) // Our main brand color
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid } // Getting my unique ID from Firebase

    // This block sorts the lessons so the one happening soonest is at the start
    val homeScreenLessons = remember(allLessons) {
        // Defining how to read the date and time strings from the database
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)

        allLessons
            .filter {
                // Only show lessons that haven't happened yet and aren't cancelled
                !it.isPast() && it.status != "Cancelled"
            }
            .sortedBy { lesson ->
                try {
                    // Combine date and time into one object so we can sort them properly
                    LocalDateTime.parse("${lesson.date} ${lesson.time}", formatter)
                } catch (e: Exception) {
                    // If the date looks weird, throw it to the very end of the list
                    LocalDateTime.MAX
                }
            }
            .take(5) // Just show the top 5 so the home screen isn't too cluttered
    }

    // Checking if the app is still fetching data from the server
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Show a loading spinner while we wait
            CircularProgressIndicator(color = logoBlue)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding(), // Keeps the content from hiding under the clock/notches
            contentPadding = PaddingValues(bottom = 100.dp) // Extra space at the bottom for the nav bar
        ) {
            // --- GREETING SECTION ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        // If the name is missing, just say Hello Buddy!
                        text = "Hello, ${uiState.userName.ifEmpty { "Study Buddy" }}!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = logoBlue
                    )
                    Text(
                        text = "Ready to learn something new?",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // --- UPCOMING LESSONS SECTION ---
            item {
                Text(
                    text = "Upcoming Lessons",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                // If no lessons are scheduled, show a simple text message
                if (homeScreenLessons.isEmpty()) {
                    Text(
                        text = "No upcoming lessons yet.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()) // Allow swiping left/right through lessons
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        homeScreenLessons.forEach { lesson ->
                            // If I am the tutor, show the student's name, otherwise show the tutor's name
                            val displayPartnerName = if (lesson.tutorId == currentUserId) {
                                lesson.studentName
                            } else {
                                lesson.tutorName
                            }

                            // Creating the actual card for each lesson
                            LessonHomeCard(
                                subject = lesson.subject.ifEmpty { "Lesson" },
                                tutor = displayPartnerName.ifEmpty { "User" },
                                time = "${lesson.date}, ${lesson.time}",
                                color = logoBlue
                            )
                        }
                    }
                }
            }

            // --- RECOMMENDED TUTORS SECTION ---
            item {
                Text(
                    text = "Recommended Tutors",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // If there's nobody to recommend yet
            if (uiState.recommendedTutors.isEmpty()) {
                item {
                    Text(
                        "No recommendations yet.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                // Loop through the recommended tutors list and create a card for each
                items(
                    items = uiState.recommendedTutors,
                    key = { it.uid } // Help Compose identify each item uniquely for better performance
                ) { tutor ->
                    TutorHomeCard(tutor, logoBlue, onTutorClick)
                }
            }
        }
    }
}

@Composable
fun LessonHomeCard(subject: String, tutor: String, time: String, color: Color) {
    Card(
        modifier = Modifier
            .width(260.dp) // Fixed width so cards can be swiped horizontally
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)) // Subtly colored border
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(subject, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color, maxLines = 1)
                // A little badge that says "TODAY" or "SOON"
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if(time.contains("Today")) "TODAY" else "SOON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column {
                Text("with $tutor", fontSize = 14.sp, color = Color.Black, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Clock icon for the time
                    Icon(Icons.Default.AccessTime, null, tint = color, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(time, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun TutorHomeCard(tutor: User, color: Color, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Disabling the ripple effect for a cleaner feel
            ) { onClick(tutor.uid) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Container
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F5FF)),
                contentAlignment = Alignment.Center
            ) {
                // If they have a picture, load it. Otherwise, show their initials.
                if (!tutor.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = tutor.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(tutor.initials, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tutor Details
            Column(modifier = Modifier.weight(1f)) {
                Text(tutor.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(tutor.subjects.firstOrNull() ?: "Tutor", color = Color.Black, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Little pin icon for their location
                    Icon(Icons.Default.LocationOn, null, tint = color, modifier = Modifier.size(12.dp))
                    Text(tutor.city, color = Color.Black, fontSize = 12.sp)
                }
            }

            // Rating and Reviews Section
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Golden star icon for rating
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(String.format("%.1f", tutor.averageRating), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                }
                Text("${tutor.totalReviews} reviews", fontSize = 11.sp, color = Color.Black)
            }
        }
    }
}