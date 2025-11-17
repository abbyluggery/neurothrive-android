package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "meal_plan_items",
    foreignKeys = [
        ForeignKey(
            entity = MealPlan::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"]
        )
    ]
)
data class MealPlanItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mealPlanId: String,
    val recipeId: String,
    val dayOfWeek: Int, // 0-6 (Sunday-Saturday)
    val mealType: String, // breakfast, lunch, dinner
    val salesforceId: String
)
