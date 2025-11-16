package com.neurothrive.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _recognitionState = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val recognitionState: StateFlow<VoiceRecognitionState> = _recognitionState

    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText: StateFlow<String> = _recognizedText

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Timber.d("Ready for speech")
                        _recognitionState.value = VoiceRecognitionState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        Timber.d("Speech started")
                        _recognitionState.value = VoiceRecognitionState.Speaking
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Audio level changed - could be used for visual feedback
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Audio buffer received
                    }

                    override fun onEndOfSpeech() {
                        Timber.d("Speech ended")
                        _recognitionState.value = VoiceRecognitionState.Processing
                    }

                    override fun onError(error: Int) {
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Unknown error: $error"
                        }
                        Timber.e("Speech recognition error: $errorMessage")
                        _recognitionState.value = VoiceRecognitionState.Error(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognizedText = matches[0]
                            Timber.i("Recognized: $recognizedText")
                            _recognizedText.value = recognizedText
                            _recognitionState.value = VoiceRecognitionState.Success(recognizedText)
                        } else {
                            _recognitionState.value = VoiceRecognitionState.Error("No results")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        if (!matches.isNullOrEmpty()) {
                            Timber.d("Partial result: ${matches[0]}")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Reserved for future use
                    }
                })
            }
            Timber.i("Speech recognizer initialized")
        } else {
            Timber.w("Speech recognition not available on this device")
            _recognitionState.value = VoiceRecognitionState.Error("Speech recognition not available")
        }
    }

    fun startListening(prompt: String = "Speak now") {
        if (_recognitionState.value is VoiceRecognitionState.Listening ||
            _recognitionState.value is VoiceRecognitionState.Speaking
        ) {
            Timber.w("Already listening")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognizer?.startListening(intent)
            Timber.d("Started listening")
        } catch (e: Exception) {
            Timber.e(e, "Error starting speech recognition")
            _recognitionState.value = VoiceRecognitionState.Error(e.message ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _recognitionState.value = VoiceRecognitionState.Idle
        Timber.d("Stopped listening")
    }

    fun reset() {
        _recognitionState.value = VoiceRecognitionState.Idle
        _recognizedText.value = ""
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        Timber.i("Speech recognizer destroyed")
    }
}

sealed class VoiceRecognitionState {
    object Idle : VoiceRecognitionState()
    object Listening : VoiceRecognitionState()
    object Speaking : VoiceRecognitionState()
    object Processing : VoiceRecognitionState()
    data class Success(val text: String) : VoiceRecognitionState()
    data class Error(val message: String) : VoiceRecognitionState()
}
