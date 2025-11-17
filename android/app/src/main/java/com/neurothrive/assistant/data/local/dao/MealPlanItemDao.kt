package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.MealPlanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanItemDao {
    @Query("SELECT * FROM meal_plan_items WHERE mealPlanId = :mealPlanId ORDER BY dayOfWeek, mealType")
    fun getByMealPlanId(mealPlanId: String): Flow<List<MealPlanItem>>

    @Query("SELECT * FROM meal_plan_items WHERE id = :id")
    suspend fun getById(id: String): MealPlanItem?

    @Query("SELECT * FROM meal_plan_items WHERE mealPlanId = :mealPlanId AND dayOfWeek = :dayOfWeek")
    fun getByDay(mealPlanId: String, dayOfWeek: Int): Flow<List<MealPlanItem>>

    @Query("SELECT * FROM meal_plan_items WHERE mealPlanId = :mealPlanId AND dayOfWeek = :dayOfWeek AND mealType = :mealType")
    suspend fun getByDayAndMealType(mealPlanId: String, dayOfWeek: Int, mealType: String): MealPlanItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MealPlanItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MealPlanItem>)

    @Update
    suspend fun update(item: MealPlanItem)

    @Delete
    suspend fun delete(item: MealPlanItem)

    @Query("DELETE FROM meal_plan_items WHERE mealPlanId = :mealPlanId")
    suspend fun deleteByMealPlanId(mealPlanId: String)

    @Query("DELETE FROM meal_plan_items")
    suspend fun deleteAll()
}
