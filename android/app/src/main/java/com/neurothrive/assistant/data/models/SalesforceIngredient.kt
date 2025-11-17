package com.neurothrive.assistant.data.models

import com.google.gson.annotations.SerializedName

data class SalesforceIngredient(
    @SerializedName("Id") val id: String,
    @SerializedName("Meal__c") val recipeId: String,
    @SerializedName("Ingredient_Name__c") val ingredientName: String,
    @SerializedName("Quantity__c") val quantity: String,
    @SerializedName("Unit__c") val unit: String?
)
