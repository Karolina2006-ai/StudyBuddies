package com.example.studybuddies.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studybuddies.viewmodel.LessonsViewModel

/**
 * FINAL EMERGENCY REPAIR: Real-time Global Synchronization.
 * This version uses the Global StateFlow to ensure slots are blocked
 * and lessons appear INSTANTLY across all screens.
 */
@Composable
fun EnhancedBookingDialog(
    tutorId: String,
    viewModel: LessonsViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val logoBlue = Color(0xFF1A73E8)
    val lightBlue = Color(0xFFF0F5FF)

    // REACTIVE BINDING: This is the critical part.
    // We observe the flow that is updated by the SnapshotListener in LessonsViewModel.
    val globalLessons by viewModel.globalAllLessons.collectAsStateWithLifecycle()

    val days = listOf("Mon, Dec 27", "Tue, Dec 28", "Wed, Dec 29", "Thu, Dec 30")
    val timeSlots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM", "04:00 PM")

    var selectedDay by remember { mutableStateOf(days[0]) }
    var selectedTime by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title - Always Black per rules
                Text(
                    text = "Select Date & Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Day Selection
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    days.forEach { day ->
                        val isSelected = selectedDay == day
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedDay = day
                                    selectedTime = ""
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) logoBlue else lightBlue
                        ) {
                            Text(
                                text = day,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (isSelected) Color.White else logoBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Time Grid - Reactive Slot Blocking
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeSlots) { time ->
                        // The 'globalLessons' comes directly from the SnapshotListener.
                        // If anyone in the world books this slot, it will turn gray instantly.
                        val isOccupied = globalLessons.any {
                            it.tutorId == tutorId && it.date == selectedDay && it.time == time && it.status != "Cancelled"
                        }
                        val isSelected = selectedTime == time

                        Surface(
                            modifier = Modifier.clickable(
                                enabled = !isOccupied,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedTime = time },
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isOccupied -> Color(0xFFF1F3F4)
                                isSelected -> logoBlue
                                else -> lightBlue
                            }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = time,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        isSelected -> Color.White
                                        isOccupied -> Color.Gray
                                        else -> logoBlue
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button
                Button(
                    onClick = {
                        if (selectedTime.isNotEmpty()) {
                            // 1. Process the booking
                            onConfirm(selectedDay, selectedTime)

                            // 2. Immediate local update trigger
                            viewModel.loadLessons()

                            // 3. Close the dialog
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedTime.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = logoBlue,
                        disabledContainerColor = Color.LightGray
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Text("Confirm Booking", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}