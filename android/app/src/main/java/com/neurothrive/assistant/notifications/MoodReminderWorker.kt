package com.neurothrive.assistant.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class MoodReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderManager: MoodReminderManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val timeOfDay = inputData.getString("time_of_day") ?: return Result.failure()

            Timber.d("Showing $timeOfDay mood reminder")

            // Show the notification
            reminderManager.showNotification(timeOfDay)

            // Reschedule for tomorrow
            rescheduleForTomorrow(timeOfDay)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error showing mood reminder")
            Result.failure()
        }
    }

    private fun rescheduleForTomorrow(timeOfDay: String) {
        val (hour, tag) = when (timeOfDay.lowercase()) {
            "morning" -> Pair(MoodReminderManager.MORNING_HOUR, "morning_reminder")
            "afternoon" -> Pair(MoodReminderManager.AFTERNOON_HOUR, "afternoon_reminder")
            "evening" -> Pair(MoodReminderManager.EVENING_HOUR, "evening_reminder")
            else -> return
        }

        val scheduledTime = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentTime = Calendar.getInstance()
        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val data = Data.Builder()
            .putString("time_of_day", timeOfDay)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MoodReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Timber.d("Rescheduled $timeOfDay reminder for tomorrow at $hour:00")
    }
}
