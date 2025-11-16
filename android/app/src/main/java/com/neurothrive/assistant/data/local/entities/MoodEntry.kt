package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val moodLevel: Int, // 1-10
    val energyLevel: Int, // 1-10
    val painLevel: Int, // 1-10
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
