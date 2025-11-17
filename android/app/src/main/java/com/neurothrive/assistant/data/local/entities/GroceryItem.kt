package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = MealPlan::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class GroceryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemName: String,
    val category: String, // produce, dairy, meat, grains, etc.
    val quantity: String,
    val unit: String? = null,
    val estimatedPrice: Double? = null,
    val isPurchased: Boolean = false,
    val mealPlanId: String? = null,
    val salesforceId: String? = null,
    val syncedToSalesforce: Boolean = false
)
