package com.neurothrive.assistant.data.remote.api

import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.auth.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val oAuthManager: OAuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for OAuth endpoints
        if (originalRequest.url.encodedPath.contains("/oauth2/")) {
            return chain.proceed(originalRequest)
        }

        var accessToken = tokenStorage.getAccessToken()

        // If token is expired, try to refresh it
        if (tokenStorage.isTokenExpired()) {
            Timber.d("Token expired, attempting refresh...")
            runBlocking {
                val result = oAuthManager.refreshAccessToken()
                if (result.isSuccess) {
                    accessToken = tokenStorage.getAccessToken()
                    Timber.d("Token refreshed successfully")
                } else {
                    Timber.e("Failed to refresh token: ${result.exceptionOrNull()?.message}")
                }
            }
        }

        // Add Authorization header
        val authenticatedRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized - token might be invalid
        if (response.code == 401) {
            Timber.w("Received 401, attempting token refresh...")
            response.close()

            runBlocking {
                val result = oAuthManager.refreshAccessToken()
                if (result.isSuccess) {
                    accessToken = tokenStorage.getAccessToken()
                }
            }

            // Retry with new token
            val retryRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer ${accessToken ?: ""}")
                .header("Content-Type", "application/json")
                .build()

            return chain.proceed(retryRequest)
        }

        return response
    }
}
