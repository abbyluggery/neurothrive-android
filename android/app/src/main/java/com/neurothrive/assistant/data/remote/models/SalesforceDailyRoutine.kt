package com.neurothrive.assistant.data.remote.models

import com.google.gson.annotations.SerializedName

data class SalesforceDailyRoutine(
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("Name")
    val name: String? = null,

    @SerializedName("Routine_Date__c")
    val routineDate: String, // ISO 8601 format

    @SerializedName("Mood_Level__c")
    val moodLevel: Int,

    @SerializedName("Energy_Level__c")
    val energyLevel: Int,

    @SerializedName("Pain_Level__c")
    val painLevel: Int,

    @SerializedName("Sleep_Quality__c")
    val sleepQuality: Int? = null,

    @SerializedName("Exercise_Minutes__c")
    val exerciseMinutes: Int? = null,

    @SerializedName("Hydration_Ounces__c")
    val hydrationOunces: Int? = null,

    @SerializedName("Meals_Eaten__c")
    val mealsEaten: Int? = null,

    @SerializedName("Journal_Entry__c")
    val journalEntry: String? = null,

    @SerializedName("External_Id__c")
    val externalId: String // Local UUID for deduplication
)
