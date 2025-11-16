package com.neurothrive.assistant

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.neurothrive.assistant.data.local.AppDatabase
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
class WinEntryDaoTest {

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
    fun insertAndRetrieveWinEntry() = runTest {
        val entry = WinEntry(
            description = "Completed project successfully",
            category = "career"
        )

        database.winEntryDao().insert(entry)
        val entries = database.winEntryDao().getWinsSince(0)

        assertEquals(1, entries.size)
        assertEquals("Completed project successfully", entries[0].description)
        assertEquals("career", entries[0].category)
    }

    @Test
    fun getUnsyncedWinEntries() = runTest {
        val synced = WinEntry(description = "Win 1", syncedToSalesforce = true)
        val unsynced = WinEntry(description = "Win 2", syncedToSalesforce = false)

        database.winEntryDao().insert(synced)
        database.winEntryDao().insert(unsynced)

        val unsyncedEntries = database.winEntryDao().getUnsynced()

        assertEquals(1, unsyncedEntries.size)
        assertEquals("Win 2", unsyncedEntries[0].description)
    }
}
