package com.neurothrive.assistant

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.JobPosting
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
class JobPostingDaoTest {

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
    fun insertAndRetrieveJobPosting() = runTest {
        val job = JobPosting(
            jobTitle = "Software Engineer",
            companyName = "Tech Corp",
            url = "https://example.com/job",
            fitScore = 8.5,
            ndFriendlinessScore = 9.0
        )

        database.jobPostingDao().insert(job)
        val jobs = database.jobPostingDao().getRecentJobs(10)

        assertEquals(1, jobs.size)
        assertEquals("Software Engineer", jobs[0].jobTitle)
        assertEquals(8.5, jobs[0].fitScore, 0.01)
    }

    @Test
    fun getHighFitJobs() = runTest {
        val highFit = JobPosting(jobTitle = "Job 1", companyName = "Company A", url = "url1", fitScore = 9.0)
        val lowFit = JobPosting(jobTitle = "Job 2", companyName = "Company B", url = "url2", fitScore = 5.0)

        database.jobPostingDao().insert(highFit)
        database.jobPostingDao().insert(lowFit)

        val highFitJobs = database.jobPostingDao().getHighFitJobs(8.0)

        assertEquals(1, highFitJobs.size)
        assertEquals("Job 1", highFitJobs[0].jobTitle)
    }
}
