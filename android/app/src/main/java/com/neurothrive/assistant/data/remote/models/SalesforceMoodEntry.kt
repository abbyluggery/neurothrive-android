package com.neurothrive.assistant.data.remote.models

import com.google.gson.annotations.SerializedName

data class SalesforceMoodEntry(
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("Name")
    val name: String? = null,

    @SerializedName("Mood_Level__c")
    val moodLevel: Int,

    @SerializedName("Energy_Level__c")
    val energyLevel: Int,

    @SerializedName("Pain_Level__c")
    val painLevel: Int,

    @SerializedName("Entry_Date__c")
    val entryDate: String, // ISO 8601 format

    @SerializedName("Notes__c")
    val notes: String? = null,

    @SerializedName("External_Id__c")
    val externalId: String // Local UUID for deduplication
)

data class SalesforceCreateResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("errors")
    val errors: List<String>? = null
)

data class SalesforceQueryResponse<T>(
    @SerializedName("totalSize")
    val totalSize: Int,

    @SerializedName("done")
    val done: Boolean,

    @SerializedName("records")
    val records: List<T>
)
