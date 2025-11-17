package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mealType: String, // breakfast, lunch, dinner, snack
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val photoUri: String? = null,
    val recipeId: String? = null,
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
