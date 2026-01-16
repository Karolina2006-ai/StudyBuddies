package com.example.studybuddies.data.model

/**
 * Data model for reviews.
 * Must be in the data.model package and have default values for Firestore deserialization.
 */
data class ReviewData(
    var id: String = "",
    var userId: String = "",
    var userName: String = "Anonymous",
    var rating: Int = 5,
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
)