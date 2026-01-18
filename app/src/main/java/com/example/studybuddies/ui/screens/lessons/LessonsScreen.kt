package com.example.studybuddies.ui.screens.lessons

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.data.model.Lesson
import com.example.studybuddies.viewmodel.LessonsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Screen displaying the personal lesson calendar and booked sessions.
 * Real-time list updates are handled via derivedStateOf and LazyColumn keys.
 */
@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel // ViewModel managing the database and notification logic
) {
    val logoBlue = Color(0xFF1A73E8) // Brand identity blue
    val lightBlue = Color(0xFFF0F5FF) // Light background for the calendar card

    // Observing state from the ViewModel using lifecycle-aware collectors
    val allLessons by viewModel.lessons.collectAsStateWithLifecycle()
    val notificationPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var selectedTab by remember { mutableIntStateOf(0) } // Toggle: 0 = Upcoming, 1 = Past
    var showRemindersDialog by remember { mutableStateOf(false) } // Controls the notification settings popup

    val today = LocalDate.now()
    var currentMonthOffset by remember { mutableIntStateOf(0) } // Tracks calendar navigation (next/prev month)
    val displayDate = today.plusMonths(currentMonthOffset.toLong())
    val monthTitle = displayDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

    // Derived state re-filters the list automatically whenever the raw data or selected tab changes
    val lessonsToShow by remember(allLessons, selectedTab) {
        derivedStateOf {
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)

            val filtered = if (selectedTab == 0) {
                // Filter for future non-cancelled lessons
                allLessons.filter { it.isUpcoming() && it.status != "Cancelled" }
            } else {
                // Filter for history: past, cancelled, or finished lessons
                allLessons.filter { it.isPast() || it.status == "Cancelled" || it.status == "Completed" }
            }

            // Chronological sorting (nearest first)
            filtered.sortedBy { lesson ->
                try {
                    LocalDateTime.parse("${lesson.date} ${lesson.time}", formatter)
                } catch (e: Exception) {
                    LocalDateTime.MAX
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Lessons", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = logoBlue)
                IconButton(onClick = { showRemindersDialog = true }) {
                    Icon(Icons.Default.Notifications, "Reminders", tint = logoBlue, modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- CALENDAR HEADER ITEM ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = lightBlue),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        // Month Selector Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentMonthOffset-- }) { Icon(Icons.Default.ArrowBack, null, tint = logoBlue) }
                            Text(monthTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = logoBlue)
                            IconButton(onClick = { currentMonthOffset++ }) { Icon(Icons.Default.ArrowForward, null, tint = logoBlue) }
                        }

                        // Weekdays Row
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                                Text(it, color = logoBlue.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center)
                            }
                        }

                        // Dynamic Calendar Grid Generation
                        val firstDayOfMonth = displayDate.withDayOfMonth(1)
                        val daysInMonth = displayDate.lengthOfMonth()
                        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                        val totalSlots = startDayOfWeek + daysInMonth
                        val rows = (totalSlots + 6) / 7

                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                for (col in 0 until 7) {
                                    val dayNum = (row * 7 + col) - startDayOfWeek + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val isToday = (dayNum == today.dayOfMonth && currentMonthOffset == 0)
                                        Box(
                                            modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isToday) logoBlue else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                color = if (isToday) Color.White else Color.Black,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- TAB SWITCHER ITEM ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF3F4F6)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TabButton("Upcoming", selectedTab == 0, logoBlue) { selectedTab = 0 }
                    TabButton("Past", selectedTab == 1, logoBlue) { selectedTab = 1 }
                }
            }

            // --- LESSON LIST ---
            if (lessonsToShow.isEmpty()) {
                item {
                    Text(
                        text = "No lessons found",
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                // Using 'key' ensures that Compose can efficiently re-order or remove items from the UI
                items(lessonsToShow, key = { it.id }) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        logoBlue = logoBlue,
                        currentUserId = currentUserId,
                        onCancelLesson = { viewModel.cancelLesson(lesson.id) },
                        showCancelButton = selectedTab == 0
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }

    // --- REMINDERS DIALOG ---
    if (showRemindersDialog) {
        var week by remember { mutableStateOf(notificationPrefs.weekBefore) }
        var oneDay by remember { mutableStateOf(notificationPrefs.oneDayBefore) }

        AlertDialog(
            onDismissRequest = { showRemindersDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Lesson Reminders", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Choose when you'd like to receive notifications", fontSize = 14.sp, color = Color.Black, textAlign = TextAlign.Center)
                }
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    ReminderToggle("1 week before", week, logoBlue) { week = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    ReminderToggle("1 day before", oneDay, logoBlue) { oneDay = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNotificationPrefs(week, oneDay)
                        showRemindersDialog = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = logoBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Preferences", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
}

/**
 * Custom Card component displaying lesson details and status.
 */
@Composable
fun LessonCard(
    lesson: Lesson,
    logoBlue: Color,
    currentUserId: String?,
    onCancelLesson: () -> Unit,
    showCancelButton: Boolean
) {
    // Logic to determine whether to show the Tutor's name or Student's name based on current user role
    val displayPartnerName = if (lesson.tutorId == currentUserId) {
        lesson.studentName.ifEmpty { "Student" }
    } else {
        lesson.tutorName.ifEmpty { "Tutor" }
    }

    // Dynamic color coding based on the lesson status
    val (statusBg, statusText) = when(lesson.status) {
        "Confirmed", "Upcoming" -> Pair(Color(0xFFE6F4EA), Color(0xFF1E8E3E))
        "Pending" -> Pair(Color(0xFFFFF7E0), Color(0xFFF9AB00))
        "Completed" -> Pair(Color(0xFFF1F3F4), Color.Black)
        "Cancelled" -> Pair(Color(0xFFFCE8E6), Color(0xFFD93025))
        else -> Pair(Color.LightGray, Color.Black)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Partner Avatar (Initials)
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF0F5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = displayPartnerName.split(" ")
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("").uppercase()

                    Text(initials, color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(lesson.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text("with $displayPartnerName", color = Color.Black, fontSize = 14.sp)
                }

                // Cancellation button available only for upcoming lessons
                if (showCancelButton && lesson.status != "Cancelled") {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .zIndex(5f),
                        shape = CircleShape,
                        color = Color(0xFFFCE8E6),
                        onClick = {
                            onCancelLesson()
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Lesson",
                                tint = Color(0xFFD93025),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time and Date Labels
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp), tint = logoBlue)
                Text(lesson.date, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp), tint = logoBlue)
                Text("${lesson.time} • ${lesson.duration}", modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            Surface(color = statusBg, shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = lesson.status,
                    color = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Custom Tab Button for the switcher row.
 */
@Composable
fun RowScope.TabButton(text: String, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) activeColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

/**
 * Toggle row for the Notification Preferences dialog.
 */
@Composable
fun ReminderToggle(title: String, checked: Boolean, activeColor: Color, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor
            )
        )
    }
}