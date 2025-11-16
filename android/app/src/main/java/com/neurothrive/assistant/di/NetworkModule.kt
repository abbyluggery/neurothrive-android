package com.neurothrive.assistant.di

import com.google.gson.Gson
import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.auth.TokenStorage
import com.neurothrive.assistant.data.remote.api.AuthInterceptor
import com.neurothrive.assistant.data.remote.api.SalesforceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SalesforceRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @BaseOkHttpClient
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        @BaseOkHttpClient baseClient: OkHttpClient,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenStorage: TokenStorage,
        oAuthManager: OAuthManager
    ): AuthInterceptor {
        return AuthInterceptor(tokenStorage, oAuthManager)
    }

    @Provides
    @Singleton
    @SalesforceRetrofit
    fun provideSalesforceRetrofit(
        client: OkHttpClient,
        gson: Gson,
        tokenStorage: TokenStorage
    ): Retrofit {
        // Use instance URL from token storage, or fallback to default
        val baseUrl = tokenStorage.getInstanceUrl()
            ?: "https://abbyluggery179.my.salesforce.com"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideSalesforceApiService(
        @SalesforceRetrofit retrofit: Retrofit
    ): SalesforceApiService {
        return retrofit.create(SalesforceApiService::class.java)
    }
}
