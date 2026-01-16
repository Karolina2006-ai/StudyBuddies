package com.example.studybuddies.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.studybuddies.MainActivity
import com.example.studybuddies.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Odbieramy dane przesłane przez AlarmManager
        val title = intent.getStringExtra("title") ?: "Study Buddies"
        val message = intent.getStringExtra("message") ?: "Upcoming lesson reminder!"
        val notificationId = intent.getIntExtra("id", 0)

        // Kliknięcie w powiadomienie otworzy aplikację
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Budowanie powiadomienia
        val builder = NotificationCompat.Builder(context, "lessons_channel")
            .setSmallIcon(R.mipmap.ic_launcher) // Używamy domyślnej ikony aplikacji
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}