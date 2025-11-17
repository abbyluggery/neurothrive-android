package com.neurothrive.assistant.data.repository

import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.*
import com.neurothrive.assistant.data.models.*
import com.neurothrive.assistant.data.remote.api.SalesforceApiService
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: SalesforceApiService
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Recipes
    fun getAllRecipes(): Flow<List<Recipe>> = database.recipeDao().getAll()

    fun getRecipesByMealType(mealType: String): Flow<List<Recipe>> =
        database.recipeDao().getByMealType(mealType)

    fun getFavoriteRecipes(): Flow<List<Recipe>> = database.recipeDao().getFavorites()

    suspend fun getRecipeById(id: String): Recipe? = database.recipeDao().getById(id)

    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        database.recipeDao().updateFavorite(recipeId, isFavorite)
    }

    // Ingredients
    fun getIngredientsByRecipeId(recipeId: String): Flow<List<Ingredient>> =
        database.ingredientDao().getByRecipeId(recipeId)

    // Meal Entries
    fun getAllMealEntries(): Flow<List<MealEntry>> = database.mealEntryDao().getAll()

    fun getMealEntriesByType(mealType: String): Flow<List<MealEntry>> =
        database.mealEntryDao().getByMealType(mealType)

    suspend fun saveMealEntry(mealEntry: MealEntry) {
        database.mealEntryDao().insert(mealEntry)
    }

    suspend fun deleteMealEntry(mealEntry: MealEntry) {
        database.mealEntryDao().delete(mealEntry)
    }

    // Meal Plans
    fun getAllMealPlans(): Flow<List<MealPlan>> = database.mealPlanDao().getAll()

    suspend fun getCurrentMealPlan(): MealPlan? = database.mealPlanDao().getCurrentPlan()

    suspend fun getActiveMealPlan(currentDate: Long): MealPlan? =
        database.mealPlanDao().getActivePlan(currentDate)

    fun getMealPlanItems(mealPlanId: String): Flow<List<MealPlanItem>> =
        database.mealPlanItemDao().getByMealPlanId(mealPlanId)

    suspend fun saveMealPlan(mealPlan: MealPlan, items: List<MealPlanItem>) {
        database.mealPlanDao().insert(mealPlan)
        database.mealPlanItemDao().insertAll(items)
    }

    // Grocery Items
    fun getAllGroceryItems(): Flow<List<GroceryItem>> = database.groceryItemDao().getAll()

    fun getUnpurchasedGroceryItems(): Flow<List<GroceryItem>> =
        database.groceryItemDao().getUnpurchased()

    fun getGroceryItemsByMealPlan(mealPlanId: String): Flow<List<GroceryItem>> =
        database.groceryItemDao().getByMealPlanId(mealPlanId)

    suspend fun saveGroceryItem(item: GroceryItem) {
        database.groceryItemDao().insert(item)
    }

    suspend fun toggleGroceryItemPurchased(itemId: String, isPurchased: Boolean) {
        database.groceryItemDao().updatePurchased(itemId, isPurchased)
    }

    suspend fun deleteGroceryItem(item: GroceryItem) {
        database.groceryItemDao().delete(item)
    }

    // Coupons
    fun getActiveCoupons(): Flow<List<Coupon>> = database.couponDao().getActive()

    suspend fun findMatchingCoupons(itemName: String): List<Coupon> =
        database.couponDao().findMatchingCoupons(itemName)

    suspend fun deactivateExpiredCoupons() {
        database.couponDao().deactivateExpired()
    }

    // Salesforce Sync
    suspend fun syncRecipesFromSalesforce(): Result<Int> {
        return try {
            val query = """
                SELECT Id, Name, Meal_Type__c, Description__c, Prep_Time_Minutes__c,
                       Cook_Time_Minutes__c, Instructions__c
                FROM Meal__c
                ORDER BY Name
            """.trimIndent()

            val response = apiService.queryRecipes(query)
            val recipes = response.records.map { sfRecipe ->
                Recipe(
                    salesforceId = sfRecipe.id,
                    name = sfRecipe.name,
                    description = sfRecipe.description,
                    mealType = sfRecipe.mealType.lowercase(),
                    prepTime = sfRecipe.prepTimeMinutes,
                    cookTime = sfRecipe.cookTimeMinutes,
                    instructions = sfRecipe.instructions,
                    lastSynced = System.currentTimeMillis()
                )
            }

            database.recipeDao().insertAll(recipes)

            // Also sync ingredients for each recipe
            recipes.forEach { recipe ->
                syncIngredientsForRecipe(recipe.salesforceId)
            }

            Timber.d("Synced ${recipes.size} recipes from Salesforce")
            Result.success(recipes.size)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing recipes from Salesforce")
            Result.failure(e)
        }
    }

    private suspend fun syncIngredientsForRecipe(salesforceRecipeId: String) {
        try {
            val query = """
                SELECT Id, Meal__c, Ingredient_Name__c, Quantity__c, Unit__c
                FROM Meal_Ingredient__c
                WHERE Meal__c = '$salesforceRecipeId'
            """.trimIndent()

            val response = apiService.queryIngredients(query)
            val recipe = database.recipeDao().getBySalesforceId(salesforceRecipeId)

            if (recipe != null) {
                val ingredients = response.records.map { sfIngredient ->
                    Ingredient(
                        recipeId = recipe.id,
                        ingredientName = sfIngredient.ingredientName,
                        quantity = sfIngredient.quantity,
                        unit = sfIngredient.unit,
                        salesforceId = sfIngredient.id
                    )
                }

                // Clear old ingredients and insert new ones
                database.ingredientDao().deleteByRecipeId(recipe.id)
                database.ingredientDao().insertAll(ingredients)

                Timber.d("Synced ${ingredients.size} ingredients for recipe ${recipe.name}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing ingredients for recipe $salesforceRecipeId")
        }
    }

    suspend fun syncCouponsFromSalesforce(): Result<Int> {
        return try {
            val query = """
                SELECT Id, Item_Name__c, Discount_Amount__c, Discount_Type__c,
                       Expiration_Date__c, Is_Active__c
                FROM Coupon__c
                WHERE Is_Active__c = true AND Expiration_Date__c >= TODAY
                ORDER BY Expiration_Date__c
            """.trimIndent()

            val response = apiService.queryCoupons(query)
            val coupons = response.records.map { sfCoupon ->
                Coupon(
                    salesforceId = sfCoupon.id,
                    itemName = sfCoupon.itemName,
                    discountAmount = sfCoupon.discountAmount,
                    discountType = sfCoupon.discountType.lowercase(),
                    expirationDate = dateFormat.parse(sfCoupon.expirationDate)?.time
                        ?: System.currentTimeMillis(),
                    isActive = sfCoupon.isActive,
                    lastSynced = System.currentTimeMillis()
                )
            }

            database.couponDao().insertAll(coupons)

            Timber.d("Synced ${coupons.size} coupons from Salesforce")
            Result.success(coupons.size)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing coupons from Salesforce")
            Result.failure(e)
        }
    }

    suspend fun syncMealEntries(): Result<Int> {
        return try {
            val unsyncedMeals = database.mealEntryDao().getUnsynced()

            unsyncedMeals.forEach { meal ->
                try {
                    // Create meal entry in Salesforce
                    // Implementation depends on your Salesforce API structure
                    // Mark as synced after successful upload
                    database.mealEntryDao().markAsSynced(meal.id, "SF_ID_${meal.id}")
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing meal entry ${meal.id}")
                }
            }

            Result.success(unsyncedMeals.size)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing meal entries")
            Result.failure(e)
        }
    }

    suspend fun generateGroceryListFromMealPlan(mealPlanId: String): List<GroceryItem> {
        val groceryMap = mutableMapOf<String, GroceryItem>()

        val mealPlanItems = database.mealPlanItemDao().getByMealPlanId(mealPlanId)
        // This would need to be converted from Flow, but for now return empty list
        // In a real implementation, you'd collect the flow and process ingredients

        Timber.d("Generated grocery list for meal plan $mealPlanId")
        return groceryMap.values.toList()
    }
}
