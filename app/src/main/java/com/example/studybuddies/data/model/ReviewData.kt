package com.example.studybuddies.data.model

/**
 * Data model for reviews
 * Has default values for Firestore deserialization (so converting serialized data like for example binary streams back into usable objects)
 */
data class ReviewData(
    // Unique ID for the review itself
    var id: String = "",

    // The UID of the person who wrote the review (to link back to their profile)
    var userId: String = "",

    // We store the name at the time of writing so we don't have to fetch the User object every time
    var userName: String = "Anonymous",

    // Simple 1-5 rating system.
    var rating: Int = 5,

    // The actual feedback text.
    var comment: String = "",

    // When was this review posted? We use Long for easy sorting (newest first), so it will show the most recent reviews
    var timestamp: Long = System.currentTimeMillis()
)