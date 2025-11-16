package com.neurothrive.assistant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "job_postings")
data class JobPosting(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val jobTitle: String,
    val companyName: String,
    val url: String,
    val salaryMin: Double? = null,
    val salaryMax: Double? = null,
    val remotePolicy: String? = null,
    val description: String? = null,
    val fitScore: Double? = null,
    val ndFriendlinessScore: Double? = null,
    val greenFlags: String? = null,
    val redFlags: String? = null,
    val datePosted: Long = System.currentTimeMillis(),
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
