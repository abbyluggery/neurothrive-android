package com.neurothrive.assistant.voice

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCommandProcessor @Inject constructor() {

    /**
     * Parse voice input into a mood command
     * Supports patterns like:
     * - "my mood is 7"
     * - "mood 8 energy 6 pain 3"
     * - "I'm feeling a 7 out of 10"
     * - "mood level 8"
     */
    fun parseMoodCommand(text: String): MoodCommand? {
        val lowerText = text.lowercase().trim()

        return try {
            // Extract mood, energy, and pain levels
            val moodLevel = extractLevel(lowerText, listOf("mood", "feeling"))
            val energyLevel = extractLevel(lowerText, listOf("energy"))
            val painLevel = extractLevel(lowerText, listOf("pain"))

            if (moodLevel != null || energyLevel != null || painLevel != null) {
                MoodCommand(
                    moodLevel = moodLevel,
                    energyLevel = energyLevel,
                    painLevel = painLevel
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing mood command: $text")
            null
        }
    }

    /**
     * Parse voice input into a win entry
     * Supports patterns like:
     * - "log a win: completed the project"
     * - "add win: finished my workout"
     * - "I had a win today: got promoted"
     */
    fun parseWinCommand(text: String): WinCommand? {
        val lowerText = text.lowercase().trim()

        return try {
            // Check if this is a win command
            val winPatterns = listOf(
                "log a win",
                "add win",
                "record win",
                "i had a win",
                "new win"
            )

            val isWinCommand = winPatterns.any { lowerText.contains(it) }

            if (isWinCommand) {
                // Extract the description (everything after the pattern and colon/punctuation)
                var description = text
                winPatterns.forEach { pattern ->
                    if (lowerText.contains(pattern)) {
                        val index = lowerText.indexOf(pattern) + pattern.length
                        description = text.substring(index).trim()
                            .removePrefix(":")
                            .removePrefix(".")
                            .trim()
                    }
                }

                if (description.isNotBlank()) {
                    WinCommand(description = description)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing win command: $text")
            null
        }
    }

    /**
     * Parse voice input into a journal entry
     */
    fun parseJournalCommand(text: String): JournalCommand? {
        val lowerText = text.lowercase().trim()

        return try {
            val journalPatterns = listOf(
                "journal entry",
                "add to journal",
                "journal",
                "note"
            )

            val isJournalCommand = journalPatterns.any { lowerText.startsWith(it) }

            if (isJournalCommand || text.length > 20) {
                // If it's explicitly a journal command or long text, treat as journal
                var journalText = text
                journalPatterns.forEach { pattern ->
                    if (lowerText.startsWith(pattern)) {
                        val index = lowerText.indexOf(pattern) + pattern.length
                        journalText = text.substring(index).trim()
                            .removePrefix(":")
                            .removePrefix(".")
                            .trim()
                    }
                }

                if (journalText.isNotBlank()) {
                    JournalCommand(text = journalText)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing journal command: $text")
            null
        }
    }

    /**
     * Extract a numeric level (1-10) from text based on keywords
     */
    private fun extractLevel(text: String, keywords: List<String>): Int? {
        // Look for patterns like "mood 7", "mood is 8", "mood level 6"
        keywords.forEach { keyword ->
            val regex = Regex("$keyword\\s+(is\\s+|level\\s+)?(\\d+)")
            val match = regex.find(text)
            if (match != null) {
                val number = match.groupValues[2].toIntOrNull()
                if (number in 1..10) {
                    return number
                }
            }
        }

        // Look for "7 out of 10" pattern after keyword
        keywords.forEach { keyword ->
            if (text.contains(keyword)) {
                val regex = Regex("(\\d+)\\s+out\\s+of\\s+10")
                val match = regex.find(text)
                if (match != null) {
                    val number = match.groupValues[1].toIntOrNull()
                    if (number in 1..10) {
                        return number
                    }
                }
            }
        }

        return null
    }

    /**
     * Determine the type of command from voice input
     */
    fun classifyCommand(text: String): CommandType {
        val lowerText = text.lowercase()

        return when {
            lowerText.contains("mood") || lowerText.contains("energy") || lowerText.contains("pain") -> {
                CommandType.MOOD
            }
            lowerText.contains("win") -> {
                CommandType.WIN
            }
            lowerText.contains("journal") || lowerText.contains("note") -> {
                CommandType.JOURNAL
            }
            lowerText.contains("sync") -> {
                CommandType.SYNC
            }
            else -> {
                CommandType.UNKNOWN
            }
        }
    }
}

data class MoodCommand(
    val moodLevel: Int?,
    val energyLevel: Int?,
    val painLevel: Int?
)

data class WinCommand(
    val description: String,
    val category: String? = null
)

data class JournalCommand(
    val text: String
)

enum class CommandType {
    MOOD,
    WIN,
    JOURNAL,
    SYNC,
    UNKNOWN
}
