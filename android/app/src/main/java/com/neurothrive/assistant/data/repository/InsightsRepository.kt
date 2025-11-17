package com.neurothrive.assistant.data.repository

import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.remote.api.ClaudeApiService
import com.neurothrive.assistant.data.remote.api.ClaudeMessage
import com.neurothrive.assistant.data.remote.api.ClaudeRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class InsightData(
    val type: String, // mood, wins, therapy
    val title: String,
    val insight: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class InsightsRepository @Inject constructor(
    private val database: AppDatabase,
    private val claudeApiService: ClaudeApiService
) {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    suspend fun generateAllInsights(): Result<List<InsightData>> = coroutineScope {
        try {
            val insights = mutableListOf<InsightData>()

            // Generate insights in parallel
            val moodInsightDeferred = async { generateMoodInsight() }
            val winsInsightDeferred = async { generateWinsInsight() }
            val therapyInsightDeferred = async { generateTherapyInsight() }

            // Collect results
            moodInsightDeferred.await()?.let { insights.add(it) }
            winsInsightDeferred.await()?.let { insights.add(it) }
            therapyInsightDeferred.await()?.let { insights.add(it) }

            Result.success(insights)
        } catch (e: Exception) {
            Timber.e(e, "Error generating insights")
            Result.failure(e)
        }
    }

    private suspend fun generateMoodInsight(): InsightData? {
        return try {
            // Get last 7 days of mood entries
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val moodEntries = database.moodEntryDao().getAll()
            // Note: In real implementation, you'd collect the flow and filter by date

            // For now, create a sample prompt
            val prompt = """
                Based on the following mood tracking data from the past week, provide a brief insight and suggestion:

                Please analyze patterns and provide:
                1. Overall mood trend
                2. Energy levels observation
                3. One actionable suggestion for improvement

                Keep the response to 2-3 sentences, warm and encouraging.
            """.trimIndent()

            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(role = "user", content = prompt)
                ),
                max_tokens = 256
            )

            val response = claudeApiService.createMessage(request)
            val insightText = response.content.firstOrNull()?.text ?: "Unable to generate insight"

            InsightData(
                type = "mood",
                title = "Mood Trends",
                insight = insightText
            )
        } catch (e: Exception) {
            Timber.e(e, "Error generating mood insight")
            null
        }
    }

    private suspend fun generateWinsInsight(): InsightData? {
        return try {
            val wins = database.winEntryDao().getAll()
            // Note: In real implementation, you'd collect the flow

            val prompt = """
                Based on recent achievements and wins:

                Provide:
                1. Recognition of progress
                2. Pattern in successes
                3. Encouragement for continued growth

                Keep response to 2-3 sentences, positive and motivating.
            """.trimIndent()

            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(role = "user", content = prompt)
                ),
                max_tokens = 256
            )

            val response = claudeApiService.createMessage(request)
            val insightText = response.content.firstOrNull()?.text ?: "Unable to generate insight"

            InsightData(
                type = "wins",
                title = "Achievement Analysis",
                insight = insightText
            )
        } catch (e: Exception) {
            Timber.e(e, "Error generating wins insight")
            null
        }
    }

    private suspend fun generateTherapyInsight(): InsightData? {
        return try {
            val sessions = database.imposterSyndromeDao().getRecent(5)
            // Note: In real implementation, you'd collect the flow

            val prompt = """
                Based on recent therapy sessions using the Find Your Facts method:

                Provide:
                1. Progress observation
                2. Common thought patterns identified
                3. Supportive recommendation

                Keep response to 2-3 sentences, compassionate and insightful.
            """.trimIndent()

            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(role = "user", content = prompt)
                ),
                max_tokens = 256
            )

            val response = claudeApiService.createMessage(request)
            val insightText = response.content.firstOrNull()?.text ?: "Unable to generate insight"

            InsightData(
                type = "therapy",
                title = "Therapy Progress",
                insight = insightText
            )
        } catch (e: Exception) {
            Timber.e(e, "Error generating therapy insight")
            null
        }
    }

    suspend fun generateCustomInsight(userPrompt: String): Result<String> {
        return try {
            val request = ClaudeRequest(
                messages = listOf(
                    ClaudeMessage(role = "user", content = userPrompt)
                ),
                max_tokens = 512
            )

            val response = claudeApiService.createMessage(request)
            val insightText = response.content.firstOrNull()?.text
                ?: "Unable to generate insight"

            Result.success(insightText)
        } catch (e: Exception) {
            Timber.e(e, "Error generating custom insight")
            Result.failure(e)
        }
    }
}
