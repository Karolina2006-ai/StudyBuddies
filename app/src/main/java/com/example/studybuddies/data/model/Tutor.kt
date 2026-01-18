package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Tutor Data Model.
 */
data class Tutor(
    @DocumentId
    var uid: String = "", // Unique ID from Firebase Auth. Maps to the document name in Firestore.

    // Basic identity info. Keeping it consistent with the general 'User' model is smart for syncing.
    var firstName: String = "",
    var surname: String = "",

    var city: String = "",
    var university: String = "",
    var bio: String = "", // A short description where the tutor can "sell" their services.
    var telephone: String = "",

    // Using Lists for these makes it easy to add/remove tags in the UI.
    var hobbies: List<String> = emptyList(),
    var interests: List<String> = emptyList(),
    var subjects: List<String> = emptyList(), // Crucial for the search filter.

    // Statistics used to show "Social Proof" (stars and number of reviews).
    var averageRating: Double = 0.0,
    var totalReviews: Int = 0,

    // Pricing info. Using Double allows for cents (e.g., 25.50), though usually it's whole numbers.
    var hourlyRate: Double = 0.0,

    // Nullable String because a tutor might not have uploaded a photo yet.
    var profileImageUri: String? = null,

    // A nested list of ReviewData objects.
    var reviews: List<ReviewData> = emptyList()
) {
    // Helper property to get the full name without writing "$firstName $surname" everywhere.
    val fullName: String get() = "$firstName $surname".trim()

    // Used for the circular avatar placeholder if the profile image is missing.
    val initials: String get() {
        return if (firstName.isNotEmpty()) {
            firstName.take(1).uppercase()
        } else {
            "?" // Fallback if the name isn't set yet.
        }
    }
}