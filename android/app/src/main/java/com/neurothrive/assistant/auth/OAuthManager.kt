package com.neurothrive.assistant.auth

import android.content.Context
import android.net.Uri
import com.neurothrive.assistant.BuildConfig
import com.neurothrive.assistant.data.remote.models.SalesforceAuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: TokenStorage,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        // These will be configured via the Salesforce Connected App
        const val CLIENT_ID = "YOUR_SALESFORCE_CONSUMER_KEY"
        const val CLIENT_SECRET = "YOUR_SALESFORCE_CONSUMER_SECRET"
        const val REDIRECT_URI = "neurothrive://oauth/callback"

        private const val AUTH_ENDPOINT = "/services/oauth2/authorize"
        private const val TOKEN_ENDPOINT = "/services/oauth2/token"
        private const val REVOKE_ENDPOINT = "/services/oauth2/revoke"
    }

    private val baseUrl = BuildConfig.SALESFORCE_BASE_URL

    /**
     * Generate the OAuth authorization URL for the user to authenticate
     */
    fun getAuthorizationUrl(): String {
        return Uri.parse("$baseUrl$AUTH_ENDPOINT").buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "api refresh_token")
            .appendQueryParameter("display", "touch")
            .build()
            .toString()
    }

    /**
     * Exchange authorization code for access token
     */
    suspend fun exchangeCodeForToken(authorizationCode: String): Result<SalesforceAuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("code", authorizationCode)
                    .add("client_id", CLIENT_ID)
                    .add("client_secret", CLIENT_SECRET)
                    .add("redirect_uri", REDIRECT_URI)
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl$TOKEN_ENDPOINT")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Timber.e("Token exchange failed: ${response.code} - ${response.message}")
                    return@withContext Result.failure(Exception("Authentication failed: ${response.message}"))
                }

                val responseBody = response.body?.string() ?: ""
                val authResponse = gson.fromJson(responseBody, SalesforceAuthResponse::class.java)

                // Save tokens securely
                tokenStorage.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    instanceUrl = authResponse.instanceUrl,
                    userId = authResponse.userId
                )

                Timber.d("Successfully authenticated with Salesforce")
                Result.success(authResponse)
            } catch (e: Exception) {
                Timber.e(e, "Error during token exchange")
                Result.failure(e)
            }
        }
    }

    /**
     * Refresh the access token using refresh token
     */
    suspend fun refreshAccessToken(): Result<SalesforceAuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenStorage.getRefreshToken()
                    ?: return@withContext Result.failure(Exception("No refresh token available"))

                val requestBody = FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("client_id", CLIENT_ID)
                    .add("client_secret", CLIENT_SECRET)
                    .add("refresh_token", refreshToken)
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl$TOKEN_ENDPOINT")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Timber.e("Token refresh failed: ${response.code}")
                    return@withContext Result.failure(Exception("Token refresh failed"))
                }

                val responseBody = response.body?.string() ?: ""
                val authResponse = gson.fromJson(responseBody, SalesforceAuthResponse::class.java)

                // Update stored tokens
                tokenStorage.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken ?: refreshToken,
                    instanceUrl = authResponse.instanceUrl,
                    userId = authResponse.userId
                )

                Timber.d("Successfully refreshed access token")
                Result.success(authResponse)
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing token")
                Result.failure(e)
            }
        }
    }

    /**
     * Revoke the access token and clear local storage
     */
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = tokenStorage.getAccessToken()

                if (!accessToken.isNullOrEmpty()) {
                    val requestBody = FormBody.Builder()
                        .add("token", accessToken)
                        .build()

                    val request = Request.Builder()
                        .url("$baseUrl$REVOKE_ENDPOINT")
                        .post(requestBody)
                        .build()

                    okHttpClient.newCall(request).execute()
                }

                tokenStorage.clearTokens()
                Timber.d("Successfully logged out")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error during logout")
                tokenStorage.clearTokens() // Clear anyway
                Result.success(Unit)
            }
        }
    }

    fun isAuthenticated(): Boolean {
        return tokenStorage.isAuthenticated()
    }
}
