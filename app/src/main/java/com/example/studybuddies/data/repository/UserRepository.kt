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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository handling user data, lessons, and communication.
 * Updated: Optimized booking logic to ensure real-time synchronization.
 */
class UserRepository(
    private val firestore: FirebaseFirestore
) {
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")
    private val lessonsCollection = firestore.collection("lessons")
    private val chatsCollection = firestore.collection("chats")

    // --- USER PROFILE ---

    suspend fun saveUserProfile(user: User) {
        try {
            usersCollection.document(user.uid).set(user).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving profile: ${e.message}")
            throw e
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            usersCollection.document(uid).get(Source.SERVER).await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching profile: ${e.message}")
            null
        }
    }

    suspend fun getAllTutors(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching tutors: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveUserAvailability(uid: String, availability: Map<String, List<String>>) {
        try {
            usersCollection.document(uid).update("availability", availability).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving availability: ${e.message}")
        }
    }

    suspend fun updateHourlyRate(uid: String, rate: Double) {
        try {
            usersCollection.document(uid).update("hourlyRate", rate).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating rate: ${e.message}")
        }
    }

    suspend fun updateNotificationSettings(uid: String, prefs: Map<String, Boolean>) {
        try {
            usersCollection.document(uid).update("notificationPrefs", prefs).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating notifications: ${e.message}")
        }
    }

    // --- REVIEWS & RATING LOGIC ---

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

    // --- BOOKING ---

    /**
     * Updated: Now uses specific document ID matching the object ID.
     * This ensures the SnapshotListener in ViewModels correctly identifies the new lesson.
     */
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

            // PROFESSIONAL FIX: Explicitly set the document ID to match the lesson ID
            lessonsCollection.document(lessonId).set(lesson).await()
            Log.d("UserRepository", "Lesson $lessonId successfully written to Firestore.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error booking lesson: ${e.message}")
        }
    }

    suspend fun getTutorLessons(tutorId: String): List<Lesson> {
        return try {
            lessonsCollection
                .whereEqualTo("tutorId", tutorId)
                .get(Source.SERVER)
                .await()
                .toObjects(Lesson::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching tutor lessons: ${e.message}")
            emptyList()
        }
    }

    // --- MESSAGING ---

    fun sendMessage(chatId: String, message: Message) {
        try {
            val chatRef = chatsCollection.document(chatId)
            chatRef.collection("messages").add(message)

            val chatMeta = mapOf(
                "lastMessage" to message.text,
                "lastMessageTimestamp" to message.timestamp,
                "participants" to listOf(message.senderId, chatId)
            )
            chatRef.set(chatMeta, SetOptions.merge())
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending message: ${e.message}")
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Message::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }
}