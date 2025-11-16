package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.MoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getEntriesBetween(startTime: Long, endTime: Long): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE salesforceId = :salesforceId LIMIT 1")
    suspend fun getBySalesforceId(salesforceId: String): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry)

    @Update
    suspend fun update(entry: MoodEntry)

    @Delete
    suspend fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}
