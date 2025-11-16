package com.neurothrive.assistant

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.MoodEntry
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
class MoodEntryDaoTest {

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
    fun insertAndRetrieveMoodEntry() = runTest {
        val entry = MoodEntry(
            moodLevel = 7,
            energyLevel = 6,
            painLevel = 2
        )

        database.moodEntryDao().insert(entry)
        val entries = database.moodEntryDao().getEntriesBetween(0, Long.MAX_VALUE)

        assertEquals(1, entries.size)
        assertEquals(7, entries[0].moodLevel)
        assertEquals(6, entries[0].energyLevel)
    }

    @Test
    fun getUnsyncedEntries() = runTest {
        val synced = MoodEntry(moodLevel = 5, energyLevel = 5, painLevel = 1, syncedToSalesforce = true)
        val unsynced = MoodEntry(moodLevel = 7, energyLevel = 8, painLevel = 0, syncedToSalesforce = false)

        database.moodEntryDao().insert(synced)
        database.moodEntryDao().insert(unsynced)

        val unsyncedEntries = database.moodEntryDao().getUnsynced()

        assertEquals(1, unsyncedEntries.size)
        assertEquals(7, unsyncedEntries[0].moodLevel)
        assertFalse(unsyncedEntries[0].syncedToSalesforce)
    }

    @Test
    fun updateMoodEntry() = runTest {
        val entry = MoodEntry(
            id = "test-id",
            moodLevel = 5,
            energyLevel = 5,
            painLevel = 3
        )

        database.moodEntryDao().insert(entry)

        val updatedEntry = entry.copy(moodLevel = 8, syncedToSalesforce = true)
        database.moodEntryDao().update(updatedEntry)

        val retrieved = database.moodEntryDao().getEntriesBetween(0, Long.MAX_VALUE)
        assertEquals(1, retrieved.size)
        assertEquals(8, retrieved[0].moodLevel)
        assertTrue(retrieved[0].syncedToSalesforce)
    }
}
