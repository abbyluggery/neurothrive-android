package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.JobPosting
import kotlinx.coroutines.flow.Flow

@Dao
interface JobPostingDao {
    @Query("SELECT * FROM job_postings ORDER BY datePosted DESC")
    fun getAllFlow(): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE fitScore >= :minScore ORDER BY fitScore DESC")
    suspend fun getHighFitJobs(minScore: Double): List<JobPosting>

    @Query("SELECT * FROM job_postings WHERE companyName = :companyName")
    suspend fun getJobsByCompany(companyName: String): List<JobPosting>

    @Query("SELECT * FROM job_postings ORDER BY datePosted DESC LIMIT :limit")
    suspend fun getRecentJobs(limit: Int): List<JobPosting>

    @Query("SELECT * FROM job_postings WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<JobPosting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobPosting)

    @Update
    suspend fun update(job: JobPosting)

    @Delete
    suspend fun delete(job: JobPosting)
}
