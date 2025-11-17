package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.GroceryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items ORDER BY category, itemName")
    fun getAll(): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE id = :id")
    suspend fun getById(id: String): GroceryItem?

    @Query("SELECT * FROM grocery_items WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<GroceryItem>

    @Query("SELECT * FROM grocery_items WHERE mealPlanId = :mealPlanId ORDER BY category, itemName")
    fun getByMealPlanId(mealPlanId: String): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE isPurchased = 0 ORDER BY category, itemName")
    fun getUnpurchased(): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY itemName")
    fun getByCategory(category: String): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GroceryItem>)

    @Update
    suspend fun update(item: GroceryItem)

    @Delete
    suspend fun delete(item: GroceryItem)

    @Query("DELETE FROM grocery_items")
    suspend fun deleteAll()

    @Query("UPDATE grocery_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchased(id: String, isPurchased: Boolean)

    @Query("UPDATE grocery_items SET syncedToSalesforce = 1, salesforceId = :salesforceId WHERE id = :id")
    suspend fun markAsSynced(id: String, salesforceId: String)
}
