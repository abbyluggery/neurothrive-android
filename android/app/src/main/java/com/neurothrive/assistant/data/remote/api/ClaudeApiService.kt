package com.neurothrive.assistant.data.remote.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClaudeApiService {
    @Headers("anthropic-version: 2023-06-01")
    @POST("v1/messages")
    suspend fun createMessage(@Body request: ClaudeRequest): ClaudeResponse
}

data class ClaudeRequest(
    val model: String = "claude-3-5-sonnet-20241022",
    val max_tokens: Int = 1024,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

data class ClaudeResponse(
    val id: String,
    val type: String = "message",
    val role: String = "assistant",
    val content: List<ClaudeContent>,
    val model: String,
    val stop_reason: String?,
    val usage: ClaudeUsage?
)

data class ClaudeContent(
    val type: String = "text",
    val text: String
)

data class ClaudeUsage(
    val input_tokens: Int,
    val output_tokens: Int
)
