package com.neurothrive.assistant.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "neurothrive_oauth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_INSTANCE_URL = "instance_url"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_ISSUED_AT = "issued_at"
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        instanceUrl: String,
        userId: String,
        issuedAt: Long = System.currentTimeMillis()
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putString(KEY_INSTANCE_URL, instanceUrl)
            putString(KEY_USER_ID, userId)
            putLong(KEY_ISSUED_AT, issuedAt)
            // Salesforce tokens typically expire after 2 hours
            putLong(KEY_TOKEN_EXPIRY, issuedAt + (2 * 60 * 60 * 1000))
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getInstanceUrl(): String? {
        return sharedPreferences.getString(KEY_INSTANCE_URL, null)
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun getTokenExpiry(): Long {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
    }

    fun isTokenExpired(): Boolean {
        val expiry = getTokenExpiry()
        return expiry == 0L || System.currentTimeMillis() >= expiry
    }

    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrEmpty() && !isTokenExpired()
    }

    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }

    fun isAuthenticated(): Boolean {
        return hasValidToken()
    }
}
