package com.neurothrive.assistant.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neurothrive.assistant.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "neurothrive_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMoodEntryDao(database: AppDatabase) = database.moodEntryDao()

    @Provides
    @Singleton
    fun provideWinEntryDao(database: AppDatabase) = database.winEntryDao()

    @Provides
    @Singleton
    fun provideJobPostingDao(database: AppDatabase) = database.jobPostingDao()

    @Provides
    @Singleton
    fun provideDailyRoutineDao(database: AppDatabase) = database.dailyRoutineDao()

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
}
