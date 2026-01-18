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
    private val auth: FirebaseAuth, // The service for login/register credentials.
    private val firestore: FirebaseFirestore // The service for storing the user's profile info.
) {
    // Check if someone is already logged in so they don't have to see the login screen again.
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Logs in an existing user using Coroutines.
     */
    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            // .await() turns the Firebase "Task" into a synchronous-looking call that doesn't block the UI thread.
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) Result.success(user)
            else Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            // If the password is wrong or there's no internet, catch the error and return it.
            Result.failure(e)
        }
    }

    /**
     * Complete user registration.
     * Creates an account in Firebase Auth, and then immediately creates a profile document in Firestore.
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
            // Step 1: Create the "identity" in Firebase Authentication (Email/Password).
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return Result.failure(Exception("Registration failed: User is null"))

            // Step 2: Prepare the "Profile" data.
            // We do this because Firebase Auth only stores email/password, not things like 'city' or 'bio'.
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
                "hourlyRate" to 50.0, // We give new tutors a default rate to start with.
                "totalReviews" to 0,
                "ratingStats" to mapOf("5" to 0, "4" to 0, "3" to 0, "2" to 0, "1" to 0),
                "reviews" to emptyList<Any>(),
                "availability" to emptyMap<String, List<String>>()
            )

            // Step 3: Write the profile document to the "users" collection in Firestore.
            // We use user.uid as the document name to link Auth and Firestore perfectly.
            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Standard Firebase function to help users who forgot their password.
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
     * Simple logout function.
     */
    fun logout() {
        auth.signOut()
    }
}