package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.ImposterSyndromeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ImposterSyndromeDao {
    @Query("SELECT * FROM imposter_syndrome_sessions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ImposterSyndromeSession>>

    @Query("SELECT * FROM imposter_syndrome_sessions WHERE id = :id")
    suspend fun getById(id: String): ImposterSyndromeSession?

    @Query("SELECT * FROM imposter_syndrome_sessions WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<ImposterSyndromeSession>

    @Query("SELECT * FROM imposter_syndrome_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<ImposterSyndromeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ImposterSyndromeSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<ImposterSyndromeSession>)

    @Update
    suspend fun update(session: ImposterSyndromeSession)

    @Delete
    suspend fun delete(session: ImposterSyndromeSession)

    @Query("DELETE FROM imposter_syndrome_sessions")
    suspend fun deleteAll()

    @Query("UPDATE imposter_syndrome_sessions SET syncedToSalesforce = 1, salesforceId = :salesforceId WHERE id = :id")
    suspend fun markAsSynced(id: String, salesforceId: String)
}
