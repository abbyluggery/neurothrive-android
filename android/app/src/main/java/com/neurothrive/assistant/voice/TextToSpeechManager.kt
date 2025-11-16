package com.neurothrive.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _ttsState = MutableStateFlow<TTSState>(TTSState.Idle)
    val ttsState: StateFlow<TTSState> = _ttsState

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Timber.e("TTS language not supported")
                    _ttsState.value = TTSState.Error("Language not supported")
                    isInitialized = false
                } else {
                    Timber.i("TTS initialized successfully")
                    isInitialized = true
                    _ttsState.value = TTSState.Ready

                    // Set up utterance listener
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _ttsState.value = TTSState.Speaking
                        }

                        override fun onDone(utteranceId: String?) {
                            _ttsState.value = TTSState.Ready
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _ttsState.value = TTSState.Error("Speech error")
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _ttsState.value = TTSState.Error("Speech error: $errorCode")
                        }
                    })
                }
            } else {
                Timber.e("TTS initialization failed")
                _ttsState.value = TTSState.Error("Initialization failed")
                isInitialized = false
            }
        }
    }

    /**
     * Speak the given text
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized) {
            Timber.w("TTS not initialized")
            return
        }

        try {
            textToSpeech?.speak(text, queueMode, null, UUID.randomUUID().toString())
            Timber.d("Speaking: $text")
        } catch (e: Exception) {
            Timber.e(e, "Error speaking text")
            _ttsState.value = TTSState.Error(e.message ?: "Speech failed")
        }
    }

    /**
     * Stop speaking
     */
    fun stop() {
        textToSpeech?.stop()
        _ttsState.value = TTSState.Ready
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    /**
     * Provide audio feedback for mood entry
     */
    fun speakMoodConfirmation(mood: Int?, energy: Int?, pain: Int?) {
        val parts = mutableListOf<String>()

        mood?.let { parts.add("Mood level $it") }
        energy?.let { parts.add("Energy level $it") }
        pain?.let { parts.add("Pain level $it") }

        if (parts.isNotEmpty()) {
            val message = "Logged: ${parts.joinToString(", ")}"
            speak(message)
        }
    }

    /**
     * Provide audio feedback for win entry
     */
    fun speakWinConfirmation(description: String) {
        speak("Win logged: $description")
    }

    /**
     * Provide audio feedback for voice recognition
     */
    fun speakListeningPrompt() {
        speak("I'm listening")
    }

    /**
     * Provide error feedback
     */
    fun speakError(message: String) {
        speak("Error: $message")
    }

    /**
     * Destroy TTS engine
     */
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        Timber.i("TTS destroyed")
    }
}

sealed class TTSState {
    object Idle : TTSState()
    object Ready : TTSState()
    object Speaking : TTSState()
    data class Error(val message: String) : TTSState()
}
