package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.WinEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WinEntryDao {
    @Query("SELECT * FROM win_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<WinEntry>>

    @Query("SELECT * FROM win_entries WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getWinsSince(startTime: Long): List<WinEntry>

    @Query("SELECT * FROM win_entries WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<WinEntry>

    @Query("SELECT * FROM win_entries WHERE salesforceId = :salesforceId LIMIT 1")
    suspend fun getBySalesforceId(salesforceId: String): WinEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WinEntry)

    @Update
    suspend fun update(entry: WinEntry)

    @Delete
    suspend fun delete(entry: WinEntry)

    @Query("DELETE FROM win_entries")
    suspend fun deleteAll()
}
