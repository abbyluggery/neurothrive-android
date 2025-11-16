package com.neurothrive.assistant.data.remote.models

import com.google.gson.annotations.SerializedName

data class SalesforceWinEntry(
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("Name")
    val name: String? = null,

    @SerializedName("Description__c")
    val description: String,

    @SerializedName("Category__c")
    val category: String? = null,

    @SerializedName("Win_Date__c")
    val winDate: String, // ISO 8601 format

    @SerializedName("External_Id__c")
    val externalId: String // Local UUID for deduplication
)
