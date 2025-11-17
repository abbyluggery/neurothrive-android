package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "imposter_syndrome_sessions")
data class ImposterSyndromeSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val thoughtText: String,
    val believabilityBefore: Int, // 1-10
    val evidenceFor: String? = null,
    val evidenceAgainst: String? = null,
    val alternativePerspective: String? = null,
    val reframeSuggestion: String? = null,
    val believabilityAfter: Int? = null, // 1-10
    val patternDetected: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
