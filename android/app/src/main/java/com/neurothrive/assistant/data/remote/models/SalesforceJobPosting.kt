package com.neurothrive.assistant.data.remote.models

import com.google.gson.annotations.SerializedName

data class SalesforceJobPosting(
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("Name")
    val name: String? = null,

    @SerializedName("Job_Title__c")
    val jobTitle: String,

    @SerializedName("Company_Name__c")
    val companyName: String,

    @SerializedName("URL__c")
    val url: String,

    @SerializedName("Salary_Min__c")
    val salaryMin: Double? = null,

    @SerializedName("Salary_Max__c")
    val salaryMax: Double? = null,

    @SerializedName("Remote_Policy__c")
    val remotePolicy: String? = null,

    @SerializedName("Description__c")
    val description: String? = null,

    @SerializedName("Fit_Score__c")
    val fitScore: Double? = null,

    @SerializedName("ND_Friendliness_Score__c")
    val ndFriendlinessScore: Double? = null,

    @SerializedName("Green_Flags__c")
    val greenFlags: String? = null,

    @SerializedName("Red_Flags__c")
    val redFlags: String? = null,

    @SerializedName("Date_Posted__c")
    val datePosted: String, // ISO 8601 format

    @SerializedName("External_Id__c")
    val externalId: String // Local UUID for deduplication
)
