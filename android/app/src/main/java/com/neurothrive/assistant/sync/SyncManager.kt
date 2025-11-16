package com.neurothrive.assistant.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val SYNC_FLEX_MINUTES = 5L
    }

    /**
     * Schedule periodic sync every 15 minutes
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = SYNC_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = SYNC_FLEX_MINUTES,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG_SYNC)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Timber.i("Periodic sync scheduled (every $SYNC_INTERVAL_MINUTES minutes)")
    }

    /**
     * Trigger immediate one-time sync
     */
    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG_SYNC)
            .build()

        workManager.enqueueUniqueWork(
            "${SyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        Timber.i("Immediate sync requested")
    }

    /**
     * Cancel all sync work
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        Timber.i("Sync cancelled")
    }

    /**
     * Get sync status as LiveData
     */
    fun getSyncStatus() = workManager.getWorkInfosByTagLiveData(SyncWorker.TAG_SYNC)

    /**
     * Check if sync is currently running
     */
    fun isSyncRunning(): Boolean {
        val workInfos = workManager.getWorkInfosByTag(SyncWorker.TAG_SYNC).get()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
}
