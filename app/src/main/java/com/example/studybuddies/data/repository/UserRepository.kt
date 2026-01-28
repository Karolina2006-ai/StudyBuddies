package com.example.studybuddies.data.repository

import android.util.Log
import com.example.studybuddies.data.model.Lesson
import com.example.studybuddies.data.model.Message
import com.example.studybuddies.data.model.ReviewData
import com.example.studybuddies.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository handling user data, lessons, and communication
 * This acts as the Single "Source of Truth" for all database operations
 */
class UserRepository(
    private val firestore: FirebaseFirestore
) {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val usersCollection = firestore.collection("users")
    private val lessonsCollection = firestore.collection("lessons")
    private val chatsCollection = firestore.collection("chats")

    // --- USER PROFILE ---

    /**
     * Overwrites or creates the user profile document in Firestore.
     */
    suspend fun saveUserProfile(user: User) {
        try {
            usersCollection.document(user.uid).set(user).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving profile: ${e.message}")
            throw e
        }
    }

    /**
     * Fetches a single user by UID. Forced to SERVER source to ensure the latest data
     */
    suspend fun getUserProfile(uid: String): User? {
        return try {
            usersCollection.document(uid).get(Source.SERVER).await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching profile: ${e.message}")
            null
        }
    }

    /**
     * Returns all users from the 'users' collection
     */
    suspend fun getAllTutors(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching tutors: ${e.message}")
            emptyList()
        }
    }

    /**
     * Saves the tutor's available time slots
     */
    suspend fun saveUserAvailability(uid: String, availability: Map<String, List<String>>) {
        try {
            usersCollection.document(uid).update("availability", availability).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving availability: ${e.message}")
        }
    }

    /**
     * Updates the tutor's price per hour.
     */
    suspend fun updateHourlyRate(uid: String, rate: Double) {
        try {
            usersCollection.document(uid).update("hourlyRate", rate).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating rate: ${e.message}")
        }
    }

    /**
     * Saves the user's notification preferences (e.g., 1 hour before lesson).
     */
    suspend fun updateNotificationSettings(uid: String, prefs: Map<String, Boolean>) {
        try {
            usersCollection.document(uid).update("notificationPrefs", prefs).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating notifications: ${e.message}")
        }
    }

    // REVIEWS & RATING LOGIC

    /**
     * Uses a 'Transaction' to ensure data integrity when adding a review
     * This recalculates the average rating and star stats atomically
     */
    suspend fun addReviewToTutor(tutorUid: String, review: ReviewData) {
        val tutorRef = usersCollection.document(tutorUid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(tutorRef)
                val user = snapshot.toObject(User::class.java) ?: return@runTransaction

                val newTotalReviews = user.totalReviews + 1
                val currentTotalScore = user.averageRating * user.totalReviews
                val newAverageRating = (currentTotalScore + review.rating.toDouble()) / newTotalReviews

                val currentStarCount = user.ratingStats[review.rating.toString()] ?: 0L
                val updatedStats = HashMap(user.ratingStats)
                updatedStats[review.rating.toString()] = currentStarCount + 1L

                val updates = hashMapOf<String, Any>(
                    "reviews" to FieldValue.arrayUnion(review),
                    "totalReviews" to newTotalReviews,
                    "averageRating" to newAverageRating,
                    "ratingStats" to updatedStats
                )

                transaction.update(tutorRef, updates)
            }.await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding review: ${e.message}")
            throw e
        }
    }

    // BOOKING & CANCELLATION

    suspend fun bookLesson(tutorUid: String, studentUid: String, day: String, time: String, subject: String) {
        try {
            val tutor = getUserProfile(tutorUid)
            val student = getUserProfile(studentUid)

            val lessonId = UUID.randomUUID().toString()
            val lesson = Lesson(
                id = lessonId,
                tutorId = tutorUid,
                studentId = studentUid,
                tutorName = tutor?.fullName ?: "Tutor",
                studentName = student?.fullName ?: "Student",
                date = day,
                time = time,
                status = "Confirmed",
                subject = subject
            )

            lessonsCollection.document(lessonId).set(lesson).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error booking lesson: ${e.message}")
        }
    }

    /**
     * Updating status to 'Cancelled'
     * This preserves historical data for the user's records
     */
    suspend fun cancelLesson(lessonId: String) {
        try {
            lessonsCollection.document(lessonId)
                .update("status", "Cancelled")
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error cancelling lesson: ${e.message}")
            throw e
        }
    }

    // PROFILE PICTURE

    /**
     * Uploads an image to Firebase Storage and returns the public download URL.
     */
    suspend fun uploadProfileImage(uid: String, imageUri: android.net.Uri): String {
        return try {
            val profileRef = storage.reference.child("profile_images/$uid.jpg")
            profileRef.putFile(imageUri).await()
            val downloadUrl = profileRef.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to upload image: ${e.message}")
            throw e
        }
    }
}