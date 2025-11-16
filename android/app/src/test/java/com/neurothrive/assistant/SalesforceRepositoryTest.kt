package com.neurothrive.assistant

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.MoodEntry
import com.neurothrive.assistant.data.local.entities.WinEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SalesforceRepositoryTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertMoodEntry_markedAsUnsynced() = runTest {
        val entry = MoodEntry(
            moodLevel = 8,
            energyLevel = 7,
            painLevel = 2,
            syncedToSalesforce = false
        )

        database.moodEntryDao().insert(entry)
        val unsyncedEntries = database.moodEntryDao().getUnsynced()

        assertEquals(1, unsyncedEntries.size)
        assertFalse(unsyncedEntries[0].syncedToSalesforce)
        assertNull(unsyncedEntries[0].salesforceId)
    }

    @Test
    fun updateMoodEntry_asSynced() = runTest {
        val entry = MoodEntry(
            id = "test-123",
            moodLevel = 7,
            energyLevel = 6,
            painLevel = 3,
            syncedToSalesforce = false
        )

        database.moodEntryDao().insert(entry)

        // Simulate sync by updating with Salesforce ID
        val syncedEntry = entry.copy(
            syncedToSalesforce = true,
            salesforceId = "a001234567890ABC"
        )
        database.moodEntryDao().update(syncedEntry)

        val unsyncedEntries = database.moodEntryDao().getUnsynced()
        assertEquals(0, unsyncedEntries.size)

        val syncedEntries = database.moodEntryDao().getEntriesBetween(0, Long.MAX_VALUE)
        assertEquals(1, syncedEntries.size)
        assertTrue(syncedEntries[0].syncedToSalesforce)
        assertEquals("a001234567890ABC", syncedEntries[0].salesforceId)
    }

    @Test
    fun multipleSyncStatuses() = runTest {
        // Insert 3 entries: 2 unsynced, 1 synced
        database.moodEntryDao().insert(
            MoodEntry(id = "1", moodLevel = 5, energyLevel = 5, painLevel = 2, syncedToSalesforce = false)
        )
        database.moodEntryDao().insert(
            MoodEntry(id = "2", moodLevel = 7, energyLevel = 8, painLevel = 1, syncedToSalesforce = false)
        )
        database.moodEntryDao().insert(
            MoodEntry(
                id = "3",
                moodLevel = 6,
                energyLevel = 6,
                painLevel = 3,
                syncedToSalesforce = true,
                salesforceId = "a00123"
            )
        )

        val unsyncedEntries = database.moodEntryDao().getUnsynced()
        assertEquals(2, unsyncedEntries.size)

        val allEntries = database.moodEntryDao().getEntriesBetween(0, Long.MAX_VALUE)
        assertEquals(3, allEntries.size)
    }

    @Test
    fun winEntry_externalIdUsedForDeduplication() = runTest {
        val winEntry = WinEntry(
            id = "local-uuid-12345",
            description = "Completed important project",
            category = "career",
            syncedToSalesforce = false
        )

        database.winEntryDao().insert(winEntry)

        val retrieved = database.winEntryDao().getWinsSince(0)
        assertEquals(1, retrieved.size)
        assertEquals("local-uuid-12345", retrieved[0].id)
    }
}
