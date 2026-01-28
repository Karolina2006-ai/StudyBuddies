package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Lesson data model
 */
@IgnoreExtraProperties // Tells Firebase: "If you see extra fields in the DB that aren't in this class, don't crash."
data class Lesson(
    @DocumentId // This maps the actual Firestore document unique ID directly to this 'id' string automatically.
    var id: String = "",
    var tutorId: String = "", // Links the lesson to a specific teacher/tutor.
    var studentId: String = "", // Links the lesson to the student who booked it.
    var tutorName: String = "", // Storing names directly makes it easier to show them in the UI without extra DB lookups.
    var studentName: String = "",
    var subject: String = "", // e.g., "Math" or "Physics".
    var date: String = "", // Saved as a String like "Jan 8, 2026" for easy display.
    var time: String = "", // Saved as "4:00 PM".
    var duration: String = "1 hour", // Default duration is set to 1 hour.
    var status: String = "Upcoming", // Tracks the lifecycle: "Confirmed", "Pending", "Completed", "Cancelled".
    var location: String = "Online" // Where the lesson happens.
) {
    /**
     * Checks if the lesson has already taken place (time has passed).
     */
    fun isPast(): Boolean {
        // If the date or time is missing, we can't compare, so just return false.
        if (date.isEmpty() || time.isEmpty()) return false
        return try {
            // We define how to read the date/time strings we stored
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)
            // Combine date and time strings into one object to compare it to 'now'
            val lessonDateTime = LocalDateTime.parse("$date $time", formatter)
            // Check if that time is strictly before the current moment
            lessonDateTime.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            // If the formatting fails, we return false so the app doesn't crash
            false
        }
    }

    /**
     * Checks if the lesson is scheduled for the future
     * It's upcoming if it's NOT in the past and the status is one of the active ones
     */
    fun isUpcoming(): Boolean {
        return !isPast() && (status == "Confirmed" || status == "Upcoming" || status == "Pending")
    }
}