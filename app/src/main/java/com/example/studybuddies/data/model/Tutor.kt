package com.example.studybuddies.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Tutor Data Model.
 */
data class Tutor(
    @DocumentId
    var uid: String = "",

    // FIX: Changed to clean names, consistent with User.kt (without @PropertyName)
    var firstName: String = "",
    var surname: String = "",

    var city: String = "",
    var university: String = "",
    var bio: String = "",
    var telephone: String = "",

    var hobbies: List<String> = emptyList(),
    var interests: List<String> = emptyList(),
    var subjects: List<String> = emptyList(),

    // This must match the field in User.kt (averageRating)
    var averageRating: Double = 0.0,

    // This must match the field in User.kt (totalReviews)
    var totalReviews: Int = 0,

    // This must match the field in User.kt (hourlyRate)
    var hourlyRate: Double = 0.0,

    // This must match the field in User.kt (profileImageUri)
    var profileImageUri: String? = null,

    // List of reviews
    var reviews: List<ReviewData> = emptyList()
) {
    // Full name helper property
    val fullName: String get() = "$firstName $surname".trim()

    // Initials helper property
    val initials: String get() {
        return if (firstName.isNotEmpty()) {
            firstName.take(1).uppercase()
        } else {
            "?"
        }
    }
}