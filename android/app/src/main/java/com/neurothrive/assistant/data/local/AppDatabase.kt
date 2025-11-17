package com.neurothrive.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neurothrive.assistant.data.local.dao.*
import com.neurothrive.assistant.data.local.entities.*

@Database(
    entities = [
        MoodEntry::class,
        WinEntry::class,
        JobPosting::class,
        DailyRoutine::class,
        ImposterSyndromeSession::class,
        MealEntry::class,
        Recipe::class,
        Ingredient::class,
        MealPlan::class,
        MealPlanItem::class,
        GroceryItem::class,
        Coupon::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Session 1-4 DAOs
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun winEntryDao(): WinEntryDao
    abstract fun jobPostingDao(): JobPostingDao
    abstract fun dailyRoutineDao(): DailyRoutineDao

    // Session 6 DAOs
    abstract fun imposterSyndromeDao(): ImposterSyndromeDao

    // Session 7 DAOs
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealPlanItemDao(): MealPlanItemDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun couponDao(): CouponDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add time fields to daily_routines
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN wakeTime TEXT")
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN sleepTime TEXT")
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN bedTime TEXT")
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN morningMood INTEGER")
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN morningEnergy INTEGER")
                database.execSQL("ALTER TABLE daily_routines ADD COLUMN morningPain INTEGER")

                // Add time of day to mood_entries
                database.execSQL("ALTER TABLE mood_entries ADD COLUMN timeOfDay TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS imposter_syndrome_sessions (
                        id TEXT PRIMARY KEY NOT NULL,
                        thoughtText TEXT NOT NULL,
                        believabilityBefore INTEGER NOT NULL,
                        evidenceFor TEXT,
                        evidenceAgainst TEXT,
                        alternativePerspective TEXT,
                        reframeSuggestion TEXT,
                        believabilityAfter INTEGER,
                        patternDetected TEXT,
                        timestamp INTEGER NOT NULL,
                        syncedToSalesforce INTEGER NOT NULL,
                        salesforceId TEXT
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Meal entries
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS meal_entries (
                        id TEXT PRIMARY KEY NOT NULL,
                        mealType TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        photoUri TEXT,
                        recipeId TEXT,
                        syncedToSalesforce INTEGER NOT NULL,
                        salesforceId TEXT
                    )
                """.trimIndent())

                // Recipes
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recipes (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        mealType TEXT NOT NULL,
                        prepTime INTEGER,
                        cookTime INTEGER,
                        instructions TEXT,
                        salesforceId TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        lastSynced INTEGER NOT NULL
                    )
                """.trimIndent())

                // Ingredients
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ingredients (
                        id TEXT PRIMARY KEY NOT NULL,
                        recipeId TEXT NOT NULL,
                        ingredientName TEXT NOT NULL,
                        quantity TEXT NOT NULL,
                        unit TEXT,
                        salesforceId TEXT NOT NULL,
                        FOREIGN KEY (recipeId) REFERENCES recipes(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Meal plans
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS meal_plans (
                        id TEXT PRIMARY KEY NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        salesforceId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Meal plan items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS meal_plan_items (
                        id TEXT PRIMARY KEY NOT NULL,
                        mealPlanId TEXT NOT NULL,
                        recipeId TEXT NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        mealType TEXT NOT NULL,
                        salesforceId TEXT NOT NULL,
                        FOREIGN KEY (mealPlanId) REFERENCES meal_plans(id) ON DELETE CASCADE,
                        FOREIGN KEY (recipeId) REFERENCES recipes(id)
                    )
                """.trimIndent())

                // Grocery items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS grocery_items (
                        id TEXT PRIMARY KEY NOT NULL,
                        itemName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        quantity TEXT NOT NULL,
                        unit TEXT,
                        estimatedPrice REAL,
                        isPurchased INTEGER NOT NULL,
                        mealPlanId TEXT,
                        salesforceId TEXT,
                        syncedToSalesforce INTEGER NOT NULL,
                        FOREIGN KEY (mealPlanId) REFERENCES meal_plans(id) ON DELETE SET NULL
                    )
                """.trimIndent())

                // Coupons
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS coupons (
                        id TEXT PRIMARY KEY NOT NULL,
                        itemName TEXT NOT NULL,
                        discountAmount REAL NOT NULL,
                        discountType TEXT NOT NULL,
                        expirationDate INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        salesforceId TEXT NOT NULL,
                        lastSynced INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neurothrive_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
