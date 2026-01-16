package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Lesson data model.
 * Updated to ensure full compatibility with Firebase real-time serialization.
 */
@IgnoreExtraProperties
data class Lesson(
    @DocumentId
    var id: String = "",
    var tutorId: String = "",
    var studentId: String = "",
    var tutorName: String = "",
    var studentName: String = "",
    var subject: String = "",
    var date: String = "",        // Format: "Jan 8, 2026"
    var time: String = "",        // Format: "4:00 PM"
    var duration: String = "1 hour",
    var status: String = "Upcoming", // "Confirmed", "Pending", "Completed", "Cancelled"
    var location: String = "Online"
) {
    /**
     * Checks if the lesson has already taken place (time has passed).
     * Maintains your original logic while adding safety for Firebase mapping.
     */
    fun isPast(): Boolean {
        if (date.isEmpty() || time.isEmpty()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)
            val lessonDateTime = LocalDateTime.parse("$date $time", formatter)
            lessonDateTime.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the lesson is scheduled for the future.
     */
    fun isUpcoming(): Boolean {
        return !isPast() && (status == "Confirmed" || status == "Upcoming" || status == "Pending")
    }
}