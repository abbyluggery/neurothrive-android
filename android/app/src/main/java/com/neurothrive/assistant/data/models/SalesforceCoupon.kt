package com.neurothrive.assistant.data.models

import com.google.gson.annotations.SerializedName

data class SalesforceCoupon(
    @SerializedName("Id") val id: String,
    @SerializedName("Item_Name__c") val itemName: String,
    @SerializedName("Discount_Amount__c") val discountAmount: Double,
    @SerializedName("Discount_Type__c") val discountType: String,
    @SerializedName("Expiration_Date__c") val expirationDate: String,
    @SerializedName("Is_Active__c") val isActive: Boolean
)
