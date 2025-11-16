package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.DailyRoutine
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRoutineDao {
    @Query("SELECT * FROM daily_routines ORDER BY date DESC")
    fun getAllFlow(): Flow<List<DailyRoutine>>

    @Query("SELECT * FROM daily_routines WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getRoutinesBetween(startDate: Long, endDate: Long): List<DailyRoutine>

    @Query("SELECT * FROM daily_routines WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<DailyRoutine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: DailyRoutine)

    @Update
    suspend fun update(routine: DailyRoutine)

    @Delete
    suspend fun delete(routine: DailyRoutine)
}
