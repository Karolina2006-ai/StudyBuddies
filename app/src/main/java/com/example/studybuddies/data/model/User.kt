package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

/**
 * User model
 * - Uses ArrayList and HashMap instead of emptyList/emptyMap for Firebase safety.
 * - All fields have default values.
 */
data class User(
    @DocumentId
    var uid: String = "",
    var email: String = "",

    // Unified field names (no first_name, just firstName)
    var firstName: String = "",
    var surname: String = "",
    var fullName: String = "",
    var role: String = "Student",

    var city: String = "",
    var university: String = "",
    var bio: String = "No bio available.",
    var telephone: String = "",

    // Using ArrayList because Firebase handles mutable lists better in this context
    var subjects: ArrayList<String> = arrayListOf(),
    var hobbies: ArrayList<String> = arrayListOf(),

    var hourlyRate: Double = 0.0,
    var averageRating: Double = 0.0,
    var totalReviews: Int = 0,

    // Rating statistics as HashMap
    var ratingStats: HashMap<String, Long> = hashMapOf(
        "1" to 0L, "2" to 0L, "3" to 0L, "4" to 0L, "5" to 0L
    ),

    var profileImageUri: String? = null,

    // List of reviews
    var reviews: ArrayList<ReviewData> = arrayListOf(),

    // Availability as HashMap
    var availability: HashMap<String, List<String>> = hashMapOf(),

    // Notification preferences
    var notificationPrefs: HashMap<String, Boolean> = hashMapOf(
        "weekBefore" to false,
        "threeDaysBefore" to true,
        "oneDayBefore" to true
    )
) {
    // Helper function for initials (safe implementation)
    val initials: String
        get() {
            return if (fullName.isNotBlank()) {
                fullName.split(" ")
                    .filter { it.isNotEmpty() }
                    .map { it.take(1) }
                    .joinToString("")
                    .uppercase()
                    .take(2)
            } else if (firstName.isNotBlank()) {
                firstName.take(1).uppercase()
            } else {
                "SB" // Fallback (StudyBuddies)
            }
        }

    val isTutor: Boolean get() = role.equals("Tutor", ignoreCase = true)

    val roleSectionTitle: String get() = if (isTutor) "Teaching" else "Studying"
}