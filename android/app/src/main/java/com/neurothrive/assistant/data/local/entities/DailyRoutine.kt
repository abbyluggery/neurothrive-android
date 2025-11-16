package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_routines")
data class DailyRoutine(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long,
    val moodLevel: Int,
    val energyLevel: Int,
    val painLevel: Int,
    val sleepQuality: Int? = null,
    val exerciseMinutes: Int? = null,
    val hydrationOunces: Int? = null,
    val mealsEaten: Int? = null,
    val journalEntry: String? = null,
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
