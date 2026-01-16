package com.example.studybuddies.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.ReviewData
import com.example.studybuddies.viewmodel.LessonsViewModel
import com.example.studybuddies.viewmodel.ProfileUiState
import com.example.studybuddies.viewmodel.TutorProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorPublicProfile(
    tutorId: String,
    navController: NavHostController,
    onBack: () -> Unit,
    lessonsViewModel: LessonsViewModel,
    tutorProfileViewModel: TutorProfileViewModel,
    onChatClick: (String, String) -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlue = Color(0xFFF0F5FF)

    val uiState by tutorProfileViewModel.uiState.collectAsStateWithLifecycle()

    // Pobieramy globalne lekcje, żeby widzieć też rezerwacje innych
    val globalLessons by lessonsViewModel.globalAllLessons.collectAsStateWithLifecycle()

    var showBookingDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // --- BLUE ALERT STATE ---
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isAlertVisible by remember { mutableStateOf(false) }

    LaunchedEffect(tutorId) {
        tutorProfileViewModel.loadTutor(tutorId)
        lessonsViewModel.loadLessons()
    }

    // Listening for ViewModel events
    LaunchedEffect(Unit) {
        lessonsViewModel.uiEvent.collect { message ->
            alertMessage = message
            isAlertVisible = true
            if (message.contains("success", ignoreCase = true)) {
                lessonsViewModel.loadLessons()
            }
            delay(2500)
            isAlertVisible = false
        }
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = logoBlue)
            }
        }
        is ProfileUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Text("Profile not found.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go Back") }
                }
            }
        }
        is ProfileUiState.Success -> {
            val tutor = state.tutor
            val hasReviews = tutor.totalReviews > 0
            val displayRating = if (hasReviews) String.format("%.1f", tutor.averageRating) else "New"

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Color.White
                ) { innerPadding ->

                    val contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // --- Header ---
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, null, tint = logoBlue)
                            }
                            Text(
                                "Tutor Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                            IconButton(onClick = { onChatClick(tutor.uid, tutor.fullName) }) {
                                Icon(Icons.Default.Chat, null, tint = logoBlue)
                            }
                        }

                        // --- Avatar & Info ---
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(110.dp).clip(CircleShape).background(lightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!tutor.profileImageUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = tutor.profileImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(tutor.initials, color = logoBlue, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(tutor.fullName, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp), color = Color.Black)

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.School, null, Modifier.size(16.dp), logoBlue)
                                Text(tutor.university, modifier = Modifier.padding(start = 6.dp), fontSize = 14.sp, color = Color.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), logoBlue)
                                Text(tutor.city, modifier = Modifier.padding(start = 6.dp), fontSize = 14.sp, color = Color.Black)
                            }
                            Text("${tutor.hourlyRate.toInt()} PLN / hour", color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                        }

                        // --- Subjects ---
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                            Text("Subjects", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            FlowRow(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                tutor.subjects.forEach { subject ->
                                    Surface(color = lightBlue, shape = RoundedCornerShape(12.dp)) {
                                        Text(subject, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // --- Bio ---
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text("Bio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            Text(tutor.bio, modifier = Modifier.padding(top = 8.dp), color = Color.Black)
                        }

                        // --- Availability (SORTED MON -> SUN) ---
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Availability", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                            Spacer(Modifier.height(16.dp))
                            val availability = tutor.availability

                            // Definiujemy poprawną kolejność dni
                            val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

                            if (availability.isNotEmpty()) {
                                // SORTUJEMY: Zamieniamy mapę na listę, sortujemy wg indeksu w daysOrder
                                availability.entries.sortedBy { entry ->
                                    val index = daysOrder.indexOf(entry.key)
                                    if (index == -1) 99 else index // Zabezpieczenie na wypadek błędnej nazwy
                                }.forEach { (day, hours) ->
                                    if (hours.isNotEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = lightBlue),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CalendarToday, null, tint = logoBlue, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(day, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    hours.forEach { time ->
                                                        Surface(
                                                            color = Color.White,
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.2f))
                                                        ) {
                                                            Text(
                                                                text = time,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                fontSize = 12.sp,
                                                                color = logoBlue
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text("No availability set", color = Color.Gray)
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 12.dp))

                        // --- Ratings & Reviews ---
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text("Ratings & Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Surface(modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), color = Color(0xFFF0F5FF), shape = RoundedCornerShape(20.dp)) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(displayRating, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = logoBlue)
                                    Text(if (hasReviews) "Based on ${tutor.totalReviews} reviews" else "No reviews yet", color = Color.Black, fontSize = 14.sp)
                                    Spacer(Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { showReviewDialog = true },
                                        modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                                        shape = RoundedCornerShape(25.dp),
                                        border = BorderStroke(1.dp, logoBlue)
                                    ) {
                                        Text("Write a Review", color = logoBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        UserReviewsList(tutor.reviews)

                        Button(
                            onClick = { showBookingDialog = true },
                            modifier = Modifier.padding(24.dp).fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = logoBlue)
                        ) {
                            Text("Book Lesson", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }

                // --- PROFESSIONAL ALERT ---
                AnimatedVisibility(
                    visible = isAlertVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        color = logoBlue,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = alertMessage ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // DIALOGS
            if (showBookingDialog) {
                key(globalLessons.size) {
                    TutorBookingDialog(
                        tutorId = tutor.uid,
                        tutorSubjects = tutor.subjects,
                        tutorAvailability = tutor.availability,
                        existingLessons = globalLessons,
                        onDismiss = { showBookingDialog = false },
                        onConfirm = { day, time, subject ->
                            lessonsViewModel.bookNewLesson(
                                tutor.uid,
                                tutor.fullName,
                                subject,
                                day,
                                time
                            )
                            showBookingDialog = false
                        }
                    )
                }
            }

            if (showReviewDialog) {
                ReviewDialog(
                    onDismiss = { showReviewDialog = false },
                    onSave = { rating, comment ->
                        lessonsViewModel.addReview(tutor.uid, rating, comment)
                        showReviewDialog = false
                        tutorProfileViewModel.loadTutor(tutorId)
                    }
                )
            }
        }
    }
}

// --- EXTRACTED COMPOSABLES ---

@Composable
fun UserReviewsList(reviews: List<ReviewData>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        if (reviews.isEmpty()) {
            Text("No reviews yet.", color = Color.Gray)
        } else {
            reviews.asReversed().take(5).forEach { review ->
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(review.userName, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Text(review.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    if (review.comment.isNotBlank()) {
                        Text(review.comment, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color(0xFFF1F1F1))
                }
            }
        }
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSave: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val logoBlue = Color(0xFF1A73E8)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Write a Review", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column {
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(Icons.Default.Star, null, tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray)
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Describe your experience...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = logoBlue)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(rating, comment) }, colors = ButtonDefaults.buttonColors(containerColor = logoBlue)) {
                Text("Submit", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorBookingDialog(
    tutorId: String,
    tutorSubjects: List<String>,
    tutorAvailability: Map<String, List<String>>,
    existingLessons: List<com.example.studybuddies.data.model.Lesson>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val today = LocalDate.now()
    val nextDays = (0..6).map { today.plusDays(it.toLong()) }
    var selectedDate by remember { mutableStateOf(nextDays[0]) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedSubject by remember { mutableStateOf(tutorSubjects.firstOrNull() ?: "Tutoring") }

    val dayOfWeekName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val availableTimes = tutorAvailability[dayOfWeekName] ?: emptyList()

    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Book a Lesson", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Select Subject:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tutorSubjects.forEach { subject ->
                        val isSelected = subject == selectedSubject
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) logoBlue else Color(0xFFF5F5F5))
                                .clickable { selectedSubject = subject }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(subject, color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Select Date:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    nextDays.forEach { date ->
                        val isSelected = date == selectedDate
                        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        val dayNum = date.dayOfMonth.toString()
                        Card(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedDate = date
                                    selectedTime = null
                                },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) logoBlue else Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(dayName, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                                Text(dayNum, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Available Times:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))

                if (availableTimes.isEmpty()) {
                    Text("No slots available.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableTimes.forEach { time ->
                            val isTaken = existingLessons.any {
                                it.tutorId == tutorId && it.date == dateStr && it.time == time && it.status != "Cancelled"
                            }
                            val isSelected = time == selectedTime
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTaken) Color.LightGray else if (isSelected) logoBlue else Color(0xFFE3F2FD))
                                    .clickable(enabled = !isTaken,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedTime = time }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = time,
                                    color = if (isSelected || isTaken) Color.White else logoBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    style = if (isTaken) androidx.compose.ui.text.TextStyle(
                                        textDecoration = TextDecoration.LineThrough
                                    ) else androidx.compose.ui.text.TextStyle()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedTime != null) onConfirm(dateStr, selectedTime!!, selectedSubject) },
                enabled = selectedTime != null,
                colors = ButtonDefaults.buttonColors(containerColor = logoBlue)
            ) { Text("Confirm", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) } }
    )
}