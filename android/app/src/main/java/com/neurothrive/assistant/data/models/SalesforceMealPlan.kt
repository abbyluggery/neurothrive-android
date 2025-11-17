package com.neurothrive.assistant.data.models

import com.google.gson.annotations.SerializedName

data class SalesforceMealPlan(
    @SerializedName("Id") val id: String,
    @SerializedName("Start_Date__c") val startDate: String,
    @SerializedName("End_Date__c") val endDate: String,
    @SerializedName("CreatedDate") val createdDate: String
)

data class SalesforceMealPlanItem(
    @SerializedName("Id") val id: String,
    @SerializedName("Meal_Plan__c") val mealPlanId: String,
    @SerializedName("Meal__c") val recipeId: String,
    @SerializedName("Day_of_Week__c") val dayOfWeek: Int,
    @SerializedName("Meal_Type__c") val mealType: String
)
