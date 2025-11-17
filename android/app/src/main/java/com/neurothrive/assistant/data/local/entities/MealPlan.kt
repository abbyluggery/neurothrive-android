package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startDate: Long,
    val endDate: Long,
    val salesforceId: String,
    val createdAt: Long = System.currentTimeMillis()
)
