package com.neurothrive.assistant.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.data.repository.SalesforceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val salesforceRepository: SalesforceRepository,
    private val oAuthManager: OAuthManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "salesforce_sync_work"
        const val TAG_SYNC = "sync"
    }

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started")

        // Check if authenticated
        if (!oAuthManager.isAuthenticated()) {
            Timber.w("Not authenticated, skipping sync")
            return Result.failure()
        }

        return try {
            // Perform sync
            val syncResult = salesforceRepository.syncAll()

            if (syncResult.isSuccess) {
                val results = syncResult.getOrNull() ?: emptyMap()
                val totalSynced = results.values.sum()

                Timber.i("Sync completed successfully: $totalSynced records synced")
                Timber.d("Sync breakdown: $results")

                Result.success()
            } else {
                Timber.e("Sync failed: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during sync")
            Result.retry()
        }
    }
}
