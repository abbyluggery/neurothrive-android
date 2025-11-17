package com.neurothrive.assistant.utils

import java.util.Calendar

enum class TimeOfDay {
    MORNING,    // 6:00 - 11:59
    AFTERNOON,  // 12:00 - 17:59
    EVENING     // 18:00 - 23:59, 0:00 - 5:59
}

object TimeUtils {
    fun getCurrentTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING
        }
    }

    fun getTimeOfDayEmoji(timeOfDay: TimeOfDay): String {
        return when (timeOfDay) {
            TimeOfDay.MORNING -> "\u2600\uFE0F" // ☀️
            TimeOfDay.AFTERNOON -> "\uD83C\uDF24\uFE0F" // 🌤️
            TimeOfDay.EVENING -> "\uD83C\uDF19" // 🌙
        }
    }

    fun getTimeOfDayLabel(timeOfDay: TimeOfDay): String {
        return when (timeOfDay) {
            TimeOfDay.MORNING -> "Morning"
            TimeOfDay.AFTERNOON -> "Afternoon"
            TimeOfDay.EVENING -> "Evening"
        }
    }

    fun getTimeOfDayGreeting(timeOfDay: TimeOfDay): String {
        return when (timeOfDay) {
            TimeOfDay.MORNING -> "Good morning! How are you feeling?"
            TimeOfDay.AFTERNOON -> "Good afternoon! Time for your check-in"
            TimeOfDay.EVENING -> "Good evening! How was your day?"
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun parseTime(timeString: String): Pair<Int, Int>? {
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        return formatTime(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }
}
