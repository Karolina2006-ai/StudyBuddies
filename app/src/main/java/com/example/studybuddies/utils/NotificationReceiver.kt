package com.example.studybuddies.utils

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.studybuddies.MainActivity
import com.example.studybuddies.R

class NotificationReceiver : BroadcastReceiver() {
    // This function triggers when the AlarmManager fires a scheduled intent
    override fun onReceive(context: Context, intent: Intent) {

        // Extracting the alert content sent by the scheduler
        val title = intent.getStringExtra("title") ?: "Study Buddies"
        val message = intent.getStringExtra("message") ?: "Upcoming lesson reminder!"

        // PGenerating or retrieving a unique ID so reminders don't overwrite each other
        val notificationId = intent.getIntExtra("id", System.currentTimeMillis().toInt())

        // Defining what happens when the student taps the notification
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            // Clears the existing task stack and starts the app fresh
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // UEnsuring the PendingIntent is unique for this specific alert
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Constructing the visual notification object
        val builder = NotificationCompat.Builder(context, "lessons_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard info icon for system alerts
            .setContentTitle(title) // The header of the notification
            .setContentText(message) // The detail body text
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures the notification pops up at the top
            .setContentIntent(pendingIntent) // Links the tap action to our app
            .setAutoCancel(true) // Removes the notification once the student clicks it

        // Vital check for Android 13 (API 33) and above
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If the student hasn't allowed notifications, we stop here to avoid an exception
            return
        }

        // Handing the builder result to the system's Notification Manager
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            // Logs any unexpected system errors during delivery
            e.printStackTrace()
        }
    }
}