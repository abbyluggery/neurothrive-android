package com.neurothrive.assistant.data.repository

import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.*
import com.neurothrive.assistant.data.remote.api.SalesforceApiService
import com.neurothrive.assistant.data.remote.models.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesforceRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: SalesforceApiService
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ==================== MOOD ENTRIES ====================

    suspend fun syncMoodEntries(): Result<Int> {
        return try {
            val unsyncedEntries = database.moodEntryDao().getUnsynced()
            var syncedCount = 0

            for (entry in unsyncedEntries) {
                val salesforceEntry = SalesforceMoodEntry(
                    moodLevel = entry.moodLevel,
                    energyLevel = entry.energyLevel,
                    painLevel = entry.painLevel,
                    entryDate = dateFormat.format(Date(entry.timestamp)),
                    notes = entry.notes,
                    externalId = entry.id
                )

                val response = if (entry.salesforceId.isNullOrEmpty()) {
                    apiService.createMoodEntry(salesforceEntry)
                } else {
                    apiService.updateMoodEntry(entry.salesforceId, salesforceEntry)
                    null // PATCH doesn't return a response body
                }

                if (response?.isSuccessful == true) {
                    val createResponse = response.body()
                    if (createResponse?.success == true) {
                        // Update local entry with Salesforce ID
                        val updatedEntry = entry.copy(
                            syncedToSalesforce = true,
                            salesforceId = createResponse.id
                        )
                        database.moodEntryDao().update(updatedEntry)
                        syncedCount++
                        Timber.d("Synced mood entry: ${entry.id}")
                    }
                } else if (response == null) {
                    // Update was successful (PATCH returns 204)
                    val updatedEntry = entry.copy(syncedToSalesforce = true)
                    database.moodEntryDao().update(updatedEntry)
                    syncedCount++
                }
            }

            Timber.i("Successfully synced $syncedCount mood entries")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing mood entries")
            Result.failure(e)
        }
    }

    // ==================== WIN ENTRIES ====================

    suspend fun syncWinEntries(): Result<Int> {
        return try {
            val unsyncedEntries = database.winEntryDao().getUnsynced()
            var syncedCount = 0

            for (entry in unsyncedEntries) {
                val salesforceEntry = SalesforceWinEntry(
                    description = entry.description,
                    category = entry.category,
                    winDate = dateFormat.format(Date(entry.timestamp)),
                    externalId = entry.id
                )

                val response = if (entry.salesforceId.isNullOrEmpty()) {
                    apiService.createWinEntry(salesforceEntry)
                } else {
                    apiService.updateWinEntry(entry.salesforceId, salesforceEntry)
                    null
                }

                if (response?.isSuccessful == true) {
                    val createResponse = response.body()
                    if (createResponse?.success == true) {
                        val updatedEntry = entry.copy(
                            syncedToSalesforce = true,
                            salesforceId = createResponse.id
                        )
                        database.winEntryDao().update(updatedEntry)
                        syncedCount++
                        Timber.d("Synced win entry: ${entry.id}")
                    }
                } else if (response == null) {
                    val updatedEntry = entry.copy(syncedToSalesforce = true)
                    database.winEntryDao().update(updatedEntry)
                    syncedCount++
                }
            }

            Timber.i("Successfully synced $syncedCount win entries")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing win entries")
            Result.failure(e)
        }
    }

    // ==================== JOB POSTINGS ====================

    suspend fun syncJobPostings(): Result<Int> {
        return try {
            val unsyncedEntries = database.jobPostingDao().getUnsynced()
            var syncedCount = 0

            for (entry in unsyncedEntries) {
                val salesforceEntry = SalesforceJobPosting(
                    jobTitle = entry.jobTitle,
                    companyName = entry.companyName,
                    url = entry.url,
                    salaryMin = entry.salaryMin,
                    salaryMax = entry.salaryMax,
                    remotePolicy = entry.remotePolicy,
                    description = entry.description,
                    fitScore = entry.fitScore,
                    ndFriendlinessScore = entry.ndFriendlinessScore,
                    greenFlags = entry.greenFlags,
                    redFlags = entry.redFlags,
                    datePosted = dateFormat.format(Date(entry.datePosted)),
                    externalId = entry.id
                )

                val response = if (entry.salesforceId.isNullOrEmpty()) {
                    apiService.createJobPosting(salesforceEntry)
                } else {
                    apiService.updateJobPosting(entry.salesforceId, salesforceEntry)
                    null
                }

                if (response?.isSuccessful == true) {
                    val createResponse = response.body()
                    if (createResponse?.success == true) {
                        val updatedEntry = entry.copy(
                            syncedToSalesforce = true,
                            salesforceId = createResponse.id
                        )
                        database.jobPostingDao().update(updatedEntry)
                        syncedCount++
                        Timber.d("Synced job posting: ${entry.id}")
                    }
                } else if (response == null) {
                    val updatedEntry = entry.copy(syncedToSalesforce = true)
                    database.jobPostingDao().update(updatedEntry)
                    syncedCount++
                }
            }

            Timber.i("Successfully synced $syncedCount job postings")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing job postings")
            Result.failure(e)
        }
    }

    // ==================== DAILY ROUTINES ====================

    suspend fun syncDailyRoutines(): Result<Int> {
        return try {
            val unsyncedEntries = database.dailyRoutineDao().getUnsynced()
            var syncedCount = 0

            for (entry in unsyncedEntries) {
                val salesforceEntry = SalesforceDailyRoutine(
                    routineDate = dateFormat.format(Date(entry.date)),
                    moodLevel = entry.moodLevel,
                    energyLevel = entry.energyLevel,
                    painLevel = entry.painLevel,
                    sleepQuality = entry.sleepQuality,
                    exerciseMinutes = entry.exerciseMinutes,
                    hydrationOunces = entry.hydrationOunces,
                    mealsEaten = entry.mealsEaten,
                    journalEntry = entry.journalEntry,
                    externalId = entry.id
                )

                val response = if (entry.salesforceId.isNullOrEmpty()) {
                    apiService.createDailyRoutine(salesforceEntry)
                } else {
                    apiService.updateDailyRoutine(entry.salesforceId, salesforceEntry)
                    null
                }

                if (response?.isSuccessful == true) {
                    val createResponse = response.body()
                    if (createResponse?.success == true) {
                        val updatedEntry = entry.copy(
                            syncedToSalesforce = true,
                            salesforceId = createResponse.id
                        )
                        database.dailyRoutineDao().update(updatedEntry)
                        syncedCount++
                        Timber.d("Synced daily routine: ${entry.id}")
                    }
                } else if (response == null) {
                    val updatedEntry = entry.copy(syncedToSalesforce = true)
                    database.dailyRoutineDao().update(updatedEntry)
                    syncedCount++
                }
            }

            Timber.i("Successfully synced $syncedCount daily routines")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing daily routines")
            Result.failure(e)
        }
    }

    // ==================== SYNC ALL ====================

    suspend fun syncAll(): Result<Map<String, Int>> {
        val results = mutableMapOf<String, Int>()

        val moodResult = syncMoodEntries()
        results["mood_entries"] = moodResult.getOrDefault(0)

        val winResult = syncWinEntries()
        results["win_entries"] = winResult.getOrDefault(0)

        val jobResult = syncJobPostings()
        results["job_postings"] = jobResult.getOrDefault(0)

        val routineResult = syncDailyRoutines()
        results["daily_routines"] = routineResult.getOrDefault(0)

        val totalSynced = results.values.sum()
        Timber.i("Total sync complete: $totalSynced records synced")

        return Result.success(results)
    }
}
