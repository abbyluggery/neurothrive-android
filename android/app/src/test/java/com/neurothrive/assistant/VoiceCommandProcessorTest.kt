package com.neurothrive.assistant

import com.neurothrive.assistant.voice.CommandType
import com.neurothrive.assistant.voice.VoiceCommandProcessor
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class VoiceCommandProcessorTest {

    private lateinit var processor: VoiceCommandProcessor

    @Before
    fun setup() {
        processor = VoiceCommandProcessor()
    }

    // ==================== MOOD COMMAND TESTS ====================

    @Test
    fun parseMoodCommand_simpleMood() {
        val result = processor.parseMoodCommand("my mood is 7")

        assertNotNull(result)
        assertEquals(7, result?.moodLevel)
        assertNull(result?.energyLevel)
        assertNull(result?.painLevel)
    }

    @Test
    fun parseMoodCommand_moodEnergyPain() {
        val result = processor.parseMoodCommand("mood 8 energy 6 pain 3")

        assertNotNull(result)
        assertEquals(8, result?.moodLevel)
        assertEquals(6, result?.energyLevel)
        assertEquals(3, result?.painLevel)
    }

    @Test
    fun parseMoodCommand_outOf10Format() {
        val result = processor.parseMoodCommand("I'm feeling a 7 out of 10")

        assertNotNull(result)
        assertEquals(7, result?.moodLevel)
    }

    @Test
    fun parseMoodCommand_moodLevel() {
        val result = processor.parseMoodCommand("mood level 8")

        assertNotNull(result)
        assertEquals(8, result?.moodLevel)
    }

    @Test
    fun parseMoodCommand_invalidNumber() {
        val result = processor.parseMoodCommand("my mood is 15")

        // Should return null for out-of-range values (1-10)
        assertNull(result)
    }

    @Test
    fun parseMoodCommand_noNumbers() {
        val result = processor.parseMoodCommand("I'm feeling great today")

        assertNull(result)
    }

    @Test
    fun parseMoodCommand_caseInsensitive() {
        val result = processor.parseMoodCommand("MY MOOD IS 9")

        assertNotNull(result)
        assertEquals(9, result?.moodLevel)
    }

    // ==================== WIN COMMAND TESTS ====================

    @Test
    fun parseWinCommand_logAWin() {
        val result = processor.parseWinCommand("log a win: completed the project")

        assertNotNull(result)
        assertEquals("completed the project", result?.description)
    }

    @Test
    fun parseWinCommand_addWin() {
        val result = processor.parseWinCommand("add win: finished my workout")

        assertNotNull(result)
        assertEquals("finished my workout", result?.description)
    }

    @Test
    fun parseWinCommand_iHadAWin() {
        val result = processor.parseWinCommand("I had a win today: got promoted")

        assertNotNull(result)
        assertEquals("got promoted", result?.description)
    }

    @Test
    fun parseWinCommand_noPunctuationAfter() {
        val result = processor.parseWinCommand("log a win finished the task")

        assertNotNull(result)
        assertEquals("finished the task", result?.description)
    }

    @Test
    fun parseWinCommand_notAWin() {
        val result = processor.parseWinCommand("my mood is 7")

        assertNull(result)
    }

    // ==================== JOURNAL COMMAND TESTS ====================

    @Test
    fun parseJournalCommand_journalEntry() {
        val result = processor.parseJournalCommand("journal entry: had a great day today")

        assertNotNull(result)
        assertEquals("had a great day today", result?.text)
    }

    @Test
    fun parseJournalCommand_journal() {
        val result = processor.parseJournalCommand("journal: feeling productive")

        assertNotNull(result)
        assertEquals("feeling productive", result?.text)
    }

    @Test
    fun parseJournalCommand_longText() {
        val longText = "Today was amazing. I accomplished so much and feel really good about my progress."
        val result = processor.parseJournalCommand(longText)

        assertNotNull(result)
        assertEquals(longText, result?.text)
    }

    @Test
    fun parseJournalCommand_shortText() {
        val result = processor.parseJournalCommand("good day")

        // Short text without journal keyword should return null
        assertNull(result)
    }

    // ==================== COMMAND CLASSIFICATION TESTS ====================

    @Test
    fun classifyCommand_mood() {
        val result = processor.classifyCommand("my mood is 7")
        assertEquals(CommandType.MOOD, result)
    }

    @Test
    fun classifyCommand_energy() {
        val result = processor.classifyCommand("energy level 8")
        assertEquals(CommandType.MOOD, result)
    }

    @Test
    fun classifyCommand_win() {
        val result = processor.classifyCommand("log a win: completed task")
        assertEquals(CommandType.WIN, result)
    }

    @Test
    fun classifyCommand_journal() {
        val result = processor.classifyCommand("journal entry: feeling good")
        assertEquals(CommandType.JOURNAL, result)
    }

    @Test
    fun classifyCommand_sync() {
        val result = processor.classifyCommand("sync my data")
        assertEquals(CommandType.SYNC, result)
    }

    @Test
    fun classifyCommand_unknown() {
        val result = processor.classifyCommand("hello there")
        assertEquals(CommandType.UNKNOWN, result)
    }
}
