package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.MealPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getById(id: String): MealPlan?

    @Query("SELECT * FROM meal_plans WHERE salesforceId = :salesforceId")
    suspend fun getBySalesforceId(salesforceId: String): MealPlan?

    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCurrentPlan(): MealPlan?

    @Query("SELECT * FROM meal_plans WHERE startDate <= :currentDate AND endDate >= :currentDate LIMIT 1")
    suspend fun getActivePlan(currentDate: Long): MealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: MealPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mealPlans: List<MealPlan>)

    @Update
    suspend fun update(mealPlan: MealPlan)

    @Delete
    suspend fun delete(mealPlan: MealPlan)

    @Query("DELETE FROM meal_plans")
    suspend fun deleteAll()
}
