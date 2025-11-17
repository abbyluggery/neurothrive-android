package com.neurothrive.assistant.data.models

import com.google.gson.annotations.SerializedName

data class SalesforceRecipe(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Meal_Type__c") val mealType: String,
    @SerializedName("Description__c") val description: String?,
    @SerializedName("Prep_Time_Minutes__c") val prepTimeMinutes: Int?,
    @SerializedName("Cook_Time_Minutes__c") val cookTimeMinutes: Int?,
    @SerializedName("Instructions__c") val instructions: String?
)
