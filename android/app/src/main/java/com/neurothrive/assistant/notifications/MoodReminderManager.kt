package com.neurothrive.assistant.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.neurothrive.assistant.R
import com.neurothrive.assistant.ui.MainActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodReminderManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "mood_reminders"
        private const val CHANNEL_NAME = "Mood Check-In Reminders"
        private const val MORNING_WORK_TAG = "morning_reminder"
        private const val AFTERNOON_WORK_TAG = "afternoon_reminder"
        private const val EVENING_WORK_TAG = "evening_reminder"

        const val MORNING_HOUR = 8  // 8 AM
        const val AFTERNOON_HOUR = 14  // 2 PM
        const val EVENING_HOUR = 19  // 7 PM
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to check in on your mood throughout the day"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllReminders() {
        scheduleMorningReminder()
        scheduleAfternoonReminder()
        scheduleEveningReminder()
    }

    fun cancelAllReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(MORNING_WORK_TAG)
        workManager.cancelAllWorkByTag(AFTERNOON_WORK_TAG)
        workManager.cancelAllWorkByTag(EVENING_WORK_TAG)
    }

    private fun scheduleMorningReminder() {
        scheduleReminder(MORNING_HOUR, 0, MORNING_WORK_TAG, "Morning")
    }

    private fun scheduleAfternoonReminder() {
        scheduleReminder(AFTERNOON_HOUR, 0, AFTERNOON_WORK_TAG, "Afternoon")
    }

    private fun scheduleEveningReminder() {
        scheduleReminder(EVENING_HOUR, 0, EVENING_WORK_TAG, "Evening")
    }

    private fun scheduleReminder(hour: Int, minute: Int, tag: String, timeOfDay: String) {
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the scheduled time is in the past, schedule for tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val data = Data.Builder()
            .putString("time_of_day", timeOfDay)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MoodReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun showNotification(timeOfDay: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "timedCheckIn")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            timeOfDay.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle("$timeOfDay Mood Check-In")
            .setContentText("How are you feeling? Take a moment to check in.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(timeOfDay.hashCode(), notification)
    }
}
