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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class NotificationPreferences(
    val weekBefore: Boolean = false,
    val threeDaysBefore: Boolean = true,
    val oneDayBefore: Boolean = true
)

// ZMIANA: Dziedziczymy po AndroidViewModel, aby mieć dostęp do 'application' (potrzebne do Alarmów)
class LessonsViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    // GLOBAL State: The "Source of Truth" for all bookings worldwide.
    private val _globalAllLessons = MutableStateFlow<List<Lesson>>(emptyList())
    val globalAllLessons: StateFlow<List<Lesson>> = _globalAllLessons.asStateFlow()

    // INTERNAL State for current user ID to handle the startup race condition
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)

    // PERSONAL List: Automatically reactive to BOTH database changes and auth state changes
    val lessons: StateFlow<List<Lesson>> = combine(_globalAllLessons, _currentUserId) { lessons, uid ->
        if (uid == null) emptyList()
        else lessons.filter { it.studentId == uid || it.tutorId == uid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _notificationPrefs = MutableStateFlow(NotificationPreferences())
    val notificationPrefs: StateFlow<NotificationPreferences> = _notificationPrefs.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private var lessonsListener: ListenerRegistration? = null

    // AuthStateListener to track user session reactively
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUserId.value = firebaseAuth.currentUser?.uid
    }

    init {
        // Registering listener and starting engine
        auth.addAuthStateListener(authStateListener)
        observeLessonsRealtime()
    }

    /**
     * GLOBAL REAL-TIME SYNC:
     * Listener is always active for slot-blocking.
     * Updates the global state, which in turn updates the personal 'lessons' flow.
     */
    private fun observeLessonsRealtime() {
        lessonsListener?.remove()

        lessonsListener = firestore.collection("lessons")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("LessonsVM", "Error listening for lessons", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allLessonsFromDb = snapshot.documents.mapNotNull { it.toObject(Lesson::class.java) }

                    // 1. Always update global state - this triggers the 'combine' and UI updates
                    _globalAllLessons.value = allLessonsFromDb

                    // 2. AUTOMATYCZNE PLANOWANIE POWIADOMIEŃ
                    // Jak tylko przyjdą dane z bazy, ustawiamy budziki w telefonie
                    scheduleNotifications(allLessonsFromDb)

                    Log.d("LessonsVM", "Real-time sync success: ${allLessonsFromDb.size} lessons synced globally.")
                }
            }
    }

    fun loadLessons() {
        observeLessonsRealtime()
    }

    fun updateNotificationPrefs(week: Boolean, threeDays: Boolean, oneDay: Boolean) {
        val newPrefs = NotificationPreferences(week, threeDays, oneDay)
        _notificationPrefs.update { newPrefs }
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val prefsMap = mapOf("weekBefore" to week, "threeDaysBefore" to threeDays, "oneDayBefore" to oneDay)
            userRepository.updateNotificationSettings(uid, prefsMap)

            // Po zmianie ustawień, przeliczamy budziki na nowo dla aktualnych lekcji
            scheduleNotifications(lessons.value)
        }
    }

    /**
     * LOGIKA POWIADOMIEŃ (ALARM MANAGER)
     * To jest nowa funkcja, która oblicza czas i ustawia alarmy systemowe.
     */
    private fun scheduleNotifications(lessons: List<Lesson>) {
        val prefs = _notificationPrefs.value
        val uid = auth.currentUser?.uid ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Filtrujemy tylko MOJE, PRZYSZŁE lekcje
        val myFutureLessons = lessons.filter {
            (it.studentId == uid || it.tutorId == uid) && !it.isPast() && it.status != "Cancelled"
        }

        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US)

        for (lesson in myFutureLessons) {
            try {
                // Konwersja daty z napisu na czas systemowy
                val lessonTime = LocalDateTime.parse("${lesson.date} ${lesson.time}", formatter)
                val lessonMillis = lessonTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Funkcja pomocnicza do ustawiania jednego konkretnego powiadomienia
                fun setAlarm(offsetMillis: Long, idSuffix: Int, msg: String) {
                    val triggerTime = lessonMillis - offsetMillis

                    // Ustawiamy tylko jeśli czas powiadomienia jest w przyszłości
                    if (triggerTime > System.currentTimeMillis()) {
                        val intent = Intent(context, NotificationReceiver::class.java).apply {
                            putExtra("title", "Upcoming Lesson: ${lesson.subject}")
                            putExtra("message", msg)
                            // Unikalne ID dla każdego powiadomienia
                            putExtra("id", lesson.id.hashCode() + idSuffix)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            lesson.id.hashCode() + idSuffix,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        try {
                            // Używamy AlarmManager do zaplanowania dokładnego czasu
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } catch (e: SecurityException) {
                            Log.e("LessonsVM", "Brak uprawnień do alarmu (SCHEDULE_EXACT_ALARM)", e)
                        }
                    }
                }

                // Sprawdzamy ustawienia suwaków
                if (prefs.weekBefore) setAlarm(7 * 24 * 3600 * 1000L, 1, "Your lesson is in 1 week!")
                if (prefs.threeDaysBefore) setAlarm(3 * 24 * 3600 * 1000L, 2, "Your lesson is in 3 days!")
                if (prefs.oneDayBefore) setAlarm(24 * 3600 * 1000L, 3, "Your lesson is tomorrow!")

                // Bonus: Zawsze przypominamy 1 godzinę przed
                setAlarm(3600 * 1000L, 4, "Lesson starts in 1 hour!")

            } catch (e: Exception) {
                Log.e("LessonsVM", "Błąd parsowania daty dla powiadomienia: ${lesson.date}", e)
            }
        }
    }

    /**
     * BULLETPROOF BOOKING ENGINE:
     * Validates against the Global Reactive State.
     */
    fun bookNewLesson(tutorUid: String, tutorName: String, subject: String, day: String, time: String) {
        viewModelScope.launch {
            val studentUid = auth.currentUser?.uid ?: return@launch

            try {
                // STEP 1: Local State Validation (Using globalAllLessons for real-time check)
                val isLocallyTaken = _globalAllLessons.value.any {
                    it.tutorId == tutorUid && it.date == day && it.time == time && it.status != "Cancelled"
                }

                if (isLocallyTaken) {
                    _uiEvent.emit("Slot already occupied by another student!")
                    return@launch
                }

                // STEP 2: Cloud Server Validation (Atomic truth check)
                val serverLessons = userRepository.getTutorLessons(tutorUid)
                val isServerTaken = serverLessons.any {
                    it.date == day && it.time == time && it.status != "Cancelled"
                }

                if (isServerTaken) {
                    _uiEvent.emit("Conflict: Slot taken by someone else just now!")
                    return@launch
                }

                // STEP 3: Safe Write
                userRepository.bookLesson(tutorUid, studentUid, day, time, subject)
                _uiEvent.emit("Lesson booked successfully!")

                // SnapshotListener automatycznie wykryje zmianę i uruchomi scheduleNotifications

            } catch (e: Exception) {
                Log.e("LessonsVM", "Booking failed", e)
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
        // Cleaning up listeners
        auth.removeAuthStateListener(authStateListener)
        lessonsListener?.remove()
    }
}