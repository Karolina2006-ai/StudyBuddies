package com.example.studybuddies.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studybuddies.data.model.Lesson
import com.example.studybuddies.data.model.ReviewData
import com.example.studybuddies.data.repository.UserRepository
import com.example.studybuddies.utils.NotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

data class NotificationPreferences(
    val weekBefore: Boolean = true,
    val oneDayBefore: Boolean = true,
    val oneHourBefore: Boolean = true
)

class LessonsViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _globalAllLessons = MutableStateFlow<List<Lesson>>(emptyList())
    // globalAllLessons remains for internal checks and validation
    val globalAllLessons: StateFlow<List<Lesson>> = _globalAllLessons.asStateFlow()

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)

    // --- REACTIVITY FIX ---
    // We use flatMapLatest to ensure that every change in _globalAllLessons
    // generates a completely fresh filtered list for the UI.
    @OptIn(ExperimentalCoroutinesApi::class)
    val lessons: StateFlow<List<Lesson>> = _currentUserId.flatMapLatest { uid ->
        _globalAllLessons.map { allLessons ->
            if (uid == null) emptyList()
            else {
                // Creating a new list via filter forces an update in Jetpack Compose
                allLessons.filter { it.studentId == uid || it.tutorId == uid }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _notificationPrefs = MutableStateFlow(NotificationPreferences())
    val notificationPrefs: StateFlow<NotificationPreferences> = _notificationPrefs.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private var lessonsListener: ListenerRegistration? = null

    // Session Guard: monitors authentication changes
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (_currentUserId.value != uid) {
            _currentUserId.value = uid
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        observeLessonsRealtime()
    }

    private fun observeLessonsRealtime() {
        lessonsListener?.remove()

        lessonsListener = firestore.collection("lessons")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("LessonsVM", "Error listening for lessons", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allLessonsFromDb = snapshot.documents.mapNotNull { doc ->
                        val lesson = doc.toObject(Lesson::class.java)
                        // Crucial: assign the Firestore document ID to the Lesson object
                        lesson?.copy(id = doc.id)
                    }
                    _globalAllLessons.value = allLessonsFromDb
                    scheduleNotifications(allLessonsFromDb)
                }
            }
    }

    fun loadLessons() {
        observeLessonsRealtime()
    }

    fun cancelLesson(lessonId: String) {
        Log.d("LessonsVM", "cancelLesson called for ID: $lessonId")
        if (lessonId.isEmpty()) return

        viewModelScope.launch {
            try {
                userRepository.cancelLesson(lessonId)
                // After a successful change in Firebase, the SnapshotListener will
                // automatically update _globalAllLessons, and our 'lessons' flow
                // will notify the UI of the status change.
                _uiEvent.emit("Lesson cancelled successfully")
            } catch (e: Exception) {
                Log.e("LessonsVM", "Failed to cancel lesson: ${e.message}")
                _uiEvent.emit("Error: Could not cancel lesson")
            }
        }
    }

    fun updateNotificationPrefs(week: Boolean, oneDay: Boolean) {
        val newPrefs = NotificationPreferences(weekBefore = week, oneDayBefore = oneDay)
        _notificationPrefs.update { newPrefs }
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val prefsMap = mapOf("weekBefore" to week, "oneDayBefore" to oneDay)
            userRepository.updateNotificationSettings(uid, prefsMap)
            scheduleNotifications(lessons.value)
        }
    }

    private fun scheduleNotifications(lessons: List<Lesson>) {
        val uid = auth.currentUser?.uid ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val myFutureLessons = lessons.filter {
            (it.studentId == uid || it.tutorId == uid) && !it.isPast() && it.status != "Cancelled"
        }

        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)

        for (lesson in myFutureLessons) {
            try {
                val lessonTime = LocalDateTime.parse("${lesson.date} ${lesson.time}", formatter)
                val lessonMillis = lessonTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val now = System.currentTimeMillis()

                fun setAlarm(offsetMillis: Long, idSuffix: Int, msg: String) {
                    val triggerTime = lessonMillis - offsetMillis
                    if (triggerTime <= now) return

                    val notificationId = abs(lesson.id.hashCode()) + idSuffix

                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("title", "Upcoming Lesson: ${lesson.subject}")
                        putExtra("message", msg)
                        putExtra("id", notificationId)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } catch (e: SecurityException) {
                        Log.e("LessonsVM", "Exact alarm permission missing", e)
                    }
                }

                setAlarm(7 * 24 * 3600 * 1000L, 101, "Your lesson starts in 1 week!")
                setAlarm(24 * 3600 * 1000L, 102, "Reminder: You have a lesson tomorrow!")
                setAlarm(3600 * 1000L, 103, "Get ready! Your lesson starts in 1 hour.")

            } catch (e: Exception) {
                Log.e("LessonsVM", "Parsing error for lesson ID: ${lesson.id}", e)
            }
        }
    }

    fun bookNewLesson(tutorUid: String, tutorName: String, subject: String, day: String, time: String) {
        viewModelScope.launch {
            val studentUid = auth.currentUser?.uid ?: return@launch
            try {
                val isLocallyTaken = _globalAllLessons.value.any {
                    it.tutorId == tutorUid && it.date == day && it.time == time && it.status != "Cancelled"
                }
                if (isLocallyTaken) {
                    _uiEvent.emit("Slot already occupied!")
                    return@launch
                }
                userRepository.bookLesson(tutorUid, studentUid, day, time, subject)
                _uiEvent.emit("Lesson booked successfully!")
            } catch (e: Exception) {
                _uiEvent.emit("Error: Booking failed.")
            }
        }
    }

    fun isSlotTaken(tutorId: String, day: String, time: String): Boolean {
        return _globalAllLessons.value.any {
            it.tutorId == tutorId && it.date == day && it.time == time && it.status != "Cancelled"
        }
    }

    fun addReview(tutorId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val studentUid = auth.currentUser?.uid ?: return@launch
            try {
                val studentProfile = userRepository.getUserProfile(studentUid)
                val safeName = studentProfile?.firstName ?: "Student"
                val review = ReviewData(
                    id = UUID.randomUUID().toString(),
                    userId = studentUid,
                    userName = safeName,
                    rating = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )
                userRepository.addReviewToTutor(tutorId, review)
                _uiEvent.emit("Review submitted successfully!")
            } catch (e: Exception) {
                _uiEvent.emit("Failed to submit review")
            }
        }
    }

    fun saveAvailability(availability: Map<String, List<String>>) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                userRepository.saveUserAvailability(uid, availability)
                _uiEvent.emit("Availability saved!")
            } catch (e: Exception) {
                _uiEvent.emit("Error saving availability")
            }
        }
    }

    /**
     * Updates the tutor's hourly rate in Firestore
     * Launches a coroutine to handle the database write operation
     */
    fun updateHourlyRate(newRate: Double) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                userRepository.updateHourlyRate(uid, newRate)
                _uiEvent.emit("Rate updated to $newRate")
            } catch (e: Exception) {
                _uiEvent.emit("Error updating rate")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        lessonsListener?.remove()
    }
}