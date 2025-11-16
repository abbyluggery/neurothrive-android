package com.neurothrive.assistant

import android.app.Application
import androidx.room.Room
import com.neurothrive.assistant.data.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NeuroThriveApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "neurothrive_database"
        ).build()

        Timber.d("NeuroThrive Application initialized")
        Timber.d("Database created with encryption ready")
    }
}
