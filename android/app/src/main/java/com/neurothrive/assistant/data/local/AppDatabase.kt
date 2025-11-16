package com.neurothrive.assistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neurothrive.assistant.data.local.dao.*
import com.neurothrive.assistant.data.local.entities.*

@Database(
    entities = [
        MoodEntry::class,
        WinEntry::class,
        JobPosting::class,
        DailyRoutine::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun winEntryDao(): WinEntryDao
    abstract fun jobPostingDao(): JobPostingDao
    abstract fun dailyRoutineDao(): DailyRoutineDao
}
