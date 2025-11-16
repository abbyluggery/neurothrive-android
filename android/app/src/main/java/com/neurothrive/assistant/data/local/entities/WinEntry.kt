package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "win_entries")
data class WinEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val description: String,
    val category: String? = null, // "career", "health", "personal"
    val timestamp: Long = System.currentTimeMillis(),
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
