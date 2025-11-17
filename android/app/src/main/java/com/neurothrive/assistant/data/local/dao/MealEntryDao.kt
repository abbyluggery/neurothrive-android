package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    suspend fun getById(id: String): MealEntry?

    @Query("SELECT * FROM meal_entries WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<MealEntry>

    @Query("SELECT * FROM meal_entries WHERE mealType = :mealType ORDER BY timestamp DESC")
    fun getByMealType(mealType: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<MealEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealEntry>)

    @Update
    suspend fun update(meal: MealEntry)

    @Delete
    suspend fun delete(meal: MealEntry)

    @Query("DELETE FROM meal_entries")
    suspend fun deleteAll()

    @Query("UPDATE meal_entries SET syncedToSalesforce = 1, salesforceId = :salesforceId WHERE id = :id")
    suspend fun markAsSynced(id: String, salesforceId: String)
}
