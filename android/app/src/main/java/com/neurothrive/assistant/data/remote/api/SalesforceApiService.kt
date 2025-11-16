package com.neurothrive.assistant.data.remote.api

import com.neurothrive.assistant.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface SalesforceApiService {

    // ==================== MOOD ENTRIES ====================

    @POST("/services/data/v59.0/sobjects/Mood_Entry__c")
    suspend fun createMoodEntry(
        @Body moodEntry: SalesforceMoodEntry
    ): Response<SalesforceCreateResponse>

    @PATCH("/services/data/v59.0/sobjects/Mood_Entry__c/{id}")
    suspend fun updateMoodEntry(
        @Path("id") salesforceId: String,
        @Body moodEntry: SalesforceMoodEntry
    ): Response<Unit>

    @GET("/services/data/v59.0/query")
    suspend fun queryMoodEntries(
        @Query("q") query: String
    ): Response<SalesforceQueryResponse<SalesforceMoodEntry>>

    // ==================== WIN ENTRIES ====================

    @POST("/services/data/v59.0/sobjects/Win_Entry__c")
    suspend fun createWinEntry(
        @Body winEntry: SalesforceWinEntry
    ): Response<SalesforceCreateResponse>

    @PATCH("/services/data/v59.0/sobjects/Win_Entry__c/{id}")
    suspend fun updateWinEntry(
        @Path("id") salesforceId: String,
        @Body winEntry: SalesforceWinEntry
    ): Response<Unit>

    @GET("/services/data/v59.0/query")
    suspend fun queryWinEntries(
        @Query("q") query: String
    ): Response<SalesforceQueryResponse<SalesforceWinEntry>>

    // ==================== JOB POSTINGS ====================

    @POST("/services/data/v59.0/sobjects/Job_Posting__c")
    suspend fun createJobPosting(
        @Body jobPosting: SalesforceJobPosting
    ): Response<SalesforceCreateResponse>

    @PATCH("/services/data/v59.0/sobjects/Job_Posting__c/{id}")
    suspend fun updateJobPosting(
        @Path("id") salesforceId: String,
        @Body jobPosting: SalesforceJobPosting
    ): Response<Unit>

    @GET("/services/data/v59.0/query")
    suspend fun queryJobPostings(
        @Query("q") query: String
    ): Response<SalesforceQueryResponse<SalesforceJobPosting>>

    // ==================== DAILY ROUTINES ====================

    @POST("/services/data/v59.0/sobjects/Daily_Routine__c")
    suspend fun createDailyRoutine(
        @Body dailyRoutine: SalesforceDailyRoutine
    ): Response<SalesforceCreateResponse>

    @PATCH("/services/data/v59.0/sobjects/Daily_Routine__c/{id}")
    suspend fun updateDailyRoutine(
        @Path("id") salesforceId: String,
        @Body dailyRoutine: SalesforceDailyRoutine
    ): Response<Unit>

    @GET("/services/data/v59.0/query")
    suspend fun queryDailyRoutines(
        @Query("q") query: String
    ): Response<SalesforceQueryResponse<SalesforceDailyRoutine>>

    // ==================== GENERAL ====================

    @GET("/services/data/v59.0/sobjects")
    suspend fun getSObjects(): Response<Any>
}
