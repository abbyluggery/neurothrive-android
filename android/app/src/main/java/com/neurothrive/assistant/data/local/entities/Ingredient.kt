package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ingredient(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val ingredientName: String,
    val quantity: String,
    val unit: String? = null,
    val salesforceId: String
)
