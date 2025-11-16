package com.neurothrive.assistant.data.remote.models

import com.google.gson.annotations.SerializedName

data class SalesforceAuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("instance_url")
    val instanceUrl: String,

    @SerializedName("id")
    val userId: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("issued_at")
    val issuedAt: String,

    @SerializedName("signature")
    val signature: String
)
