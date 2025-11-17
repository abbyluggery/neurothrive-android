package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val mealType: String, // breakfast, lunch, dinner
    val prepTime: Int? = null, // minutes
    val cookTime: Int? = null, // minutes
    val instructions: String? = null,
    val salesforceId: String,
    val isFavorite: Boolean = false,
    val lastSynced: Long = System.currentTimeMillis()
)
