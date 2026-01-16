package com.example.studybuddies.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository handling user authentication with Firebase Auth
 * and profile data synchronization in Firestore.
 */
class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Retrieves the currently logged-in Firebase user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Logs in an existing user.
     */
    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) Result.success(user)
            else Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete user registration.
     * Creates an account in Firebase Auth, and then immediately creates a profile document in Firestore.
     * Restored support for all fields required for the profile and tiles to work correctly.
     */
    suspend fun register(
        email: String,
        pass: String,
        firstName: String,
        surname: String,
        role: String,
        city: String = "",
        university: String = "",
        phone: String = "",
        bio: String = "Hey, I'm using StudyBuddies!",
        hobbies: List<String> = emptyList(),
        subjects: List<String> = emptyList()
    ): Result<FirebaseUser> {
        return try {
            // 1. Create account in Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return Result.failure(Exception("Registration failed: User is null"))

            // 2. Prepare profile data for Firestore database
            // These data are essential for tutor tiles to display correctly in the morning.
            val userData = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "firstName" to firstName,
                "surname" to surname,
                "fullName" to "$firstName $surname",
                "role" to role,
                "city" to city,
                "university" to university,
                "telephone" to phone,
                "bio" to bio,
                "hobbies" to hobbies,
                "subjects" to subjects,
                "averageRating" to 0.0,
                "hourlyRate" to 50.0, // Default rate for new tutors
                "totalReviews" to 0,
                "ratingStats" to mapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0),
                "reviews" to emptyList<Any>(),
                "availability" to emptyMap<String, List<String>>()
            )

            // 3. Save document in "users" collection
            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs out the user.
     */
    fun logout() {
        auth.signOut()
    }
}