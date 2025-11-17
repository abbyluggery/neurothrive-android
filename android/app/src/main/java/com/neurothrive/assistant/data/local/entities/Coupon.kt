package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemName: String,
    val discountAmount: Double,
    val discountType: String, // percentage, fixed
    val expirationDate: Long,
    val isActive: Boolean = true,
    val salesforceId: String,
    val lastSynced: Long = System.currentTimeMillis()
)
