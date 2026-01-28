package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

/**
 * User model
 * - Uses ArrayList and HashMap instead of emptyList/emptyMap for Firebase safety
 * - All fields have default values to avoid crashes during deserialization
 */
data class User(
    @DocumentId
    var uid: String = "", // Unique identifier from Firebase Authentication
    var email: String = "",

    // Personal details. Note: fullName is stored directly to save time
    var firstName: String = "",
    var surname: String = "",
    var fullName: String = "",
    var role: String = "Student", // Determines if the user sees the 'Tutor' or 'Student' interface

    var city: String = "",
    var university: String = "",
    var bio: String = "No bio available.",
    var telephone: String = "",

    // ArrayList is used here because it’s a concrete implementation that Firebase
    // can easily instantiate when reading data back from the cloud
    var subjects: ArrayList<String> = arrayListOf(),
    var hobbies: ArrayList<String> = arrayListOf(),

    // Professional fields (used if role == "Tutor")
    var hourlyRate: Double = 0.0,
    var averageRating: Double = 0.0,
    var totalReviews: Int = 0,

    // Stores how many 1-star, 2-star, and so on, reviews a user has
    // String keys "1"-"5" are used because Firebase Map keys must be Strings
    var ratingStats: HashMap<String, Long> = hashMapOf(
        "1" to 0L, "2" to 0L, "3" to 0L, "4" to 0L, "5" to 0L
    ),

    var profileImageUri: String? = null,

    // A list of ReviewData objects associated with this user
    var reviews: ArrayList<ReviewData> = arrayListOf(),

    // Mapping days (example: "Monday") to a list of available hours (example: ["10:00", "14:00" and so on]).
    var availability: HashMap<String, List<String>> = hashMapOf(),

    // Settings for the NotificationReceiver to check
    var notificationPrefs: HashMap<String, Boolean> = hashMapOf(
        "weekBefore" to false,
        "threeDaysBefore" to true,
        "oneDayBefore" to true
    )
) {
    /**
     * Logic to generate initials like "KH" for "Karolina Hruszowiec".
     * It splits the name, takes the first letter of each part, and limits it to 2 chars.
     */
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
                "SB" // Default fallback to "StudyBuddies"
            }
        }

    // Quick check used throughout the UI to hide/show Tutor-specific buttons (if the user is logged in as a student)
    val isTutor: Boolean get() = role.equals("Tutor", ignoreCase = true)

    // Dynamic UI label: Tutors see "Teaching", Students see "Studying"
    val roleSectionTitle: String get() = if (isTutor) "Teaching" else "Studying"
}