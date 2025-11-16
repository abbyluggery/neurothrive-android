package com.neurothrive.assistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.MoodEntry
import com.neurothrive.assistant.data.local.entities.WinEntry
import com.neurothrive.assistant.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recognitionState by viewModel.recognitionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Input") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Stop, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Status text
            Text(
                text = when (recognitionState) {
                    is VoiceRecognitionState.Idle -> "Tap the microphone to start"
                    is VoiceRecognitionState.Listening -> "Listening..."
                    is VoiceRecognitionState.Speaking -> "I hear you..."
                    is VoiceRecognitionState.Processing -> "Processing..."
                    is VoiceRecognitionState.Success -> "Got it!"
                    is VoiceRecognitionState.Error -> "Error: ${(recognitionState as VoiceRecognitionState.Error).message}"
                },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            // Recognized text
            if (uiState.recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "You said:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.recognizedText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Microphone button with animation
            VoiceMicrophoneButton(
                isListening = recognitionState is VoiceRecognitionState.Listening ||
                        recognitionState is VoiceRecognitionState.Speaking,
                onClick = {
                    if (recognitionState is VoiceRecognitionState.Listening ||
                        recognitionState is VoiceRecognitionState.Speaking
                    ) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                }
            )

            // Parsed command result
            if (uiState.parsedCommand != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Detected Command:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.parsedCommand,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Success message
            if (uiState.saveSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Saved successfully!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Try saying:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• \"My mood is 7\"", style = MaterialTheme.typography.bodyMedium)
                    Text("• \"Mood 8, energy 6, pain 2\"", style = MaterialTheme.typography.bodyMedium)
                    Text("• \"Log a win: finished my workout\"", style = MaterialTheme.typography.bodyMedium)
                    Text("• \"Journal: Had a great day today\"", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun VoiceMicrophoneButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        containerColor = if (isListening)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isListening) "Stop" else "Start",
            modifier = Modifier.size(48.dp)
        )
    }
}

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val voiceCommandProcessor: VoiceCommandProcessor,
    private val textToSpeechManager: TextToSpeechManager,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState

    val recognitionState = voiceRecognitionManager.recognitionState

    init {
        observeRecognition()
    }

    private fun observeRecognition() {
        viewModelScope.launch {
            voiceRecognitionManager.recognitionState.collect { state ->
                if (state is VoiceRecognitionState.Success) {
                    handleRecognizedText(state.text)
                }
            }
        }
    }

    fun startListening() {
        voiceRecognitionManager.startListening("Say your mood, win, or journal entry")
        textToSpeechManager.speakListeningPrompt()
        _uiState.value = _uiState.value.copy(
            recognizedText = "",
            parsedCommand = null,
            saveSuccess = false
        )
    }

    fun stopListening() {
        voiceRecognitionManager.stopListening()
    }

    private fun handleRecognizedText(text: String) {
        Timber.i("Handling recognized text: $text")
        _uiState.value = _uiState.value.copy(recognizedText = text)

        // Try to parse as different command types
        val commandType = voiceCommandProcessor.classifyCommand(text)

        when (commandType) {
            CommandType.MOOD -> {
                val moodCommand = voiceCommandProcessor.parseMoodCommand(text)
                if (moodCommand != null) {
                    saveMoodEntry(moodCommand)
                }
            }
            CommandType.WIN -> {
                val winCommand = voiceCommandProcessor.parseWinCommand(text)
                if (winCommand != null) {
                    saveWinEntry(winCommand)
                }
            }
            CommandType.JOURNAL -> {
                val journalCommand = voiceCommandProcessor.parseJournalCommand(text)
                if (journalCommand != null) {
                    _uiState.value = _uiState.value.copy(
                        parsedCommand = "Journal: ${journalCommand.text}"
                    )
                    textToSpeechManager.speak("Journal entry noted")
                }
            }
            else -> {
                _uiState.value = _uiState.value.copy(
                    parsedCommand = "Command not recognized. Try again."
                )
            }
        }
    }

    private fun saveMoodEntry(command: MoodCommand) {
        viewModelScope.launch {
            try {
                val entry = MoodEntry(
                    moodLevel = command.moodLevel ?: 5,
                    energyLevel = command.energyLevel ?: 5,
                    painLevel = command.painLevel ?: 1,
                    notes = "Voice entry: ${_uiState.value.recognizedText}"
                )

                database.moodEntryDao().insert(entry)

                val commandText = buildString {
                    append("Mood Entry: ")
                    command.moodLevel?.let { append("Mood=$it ") }
                    command.energyLevel?.let { append("Energy=$it ") }
                    command.painLevel?.let { append("Pain=$it") }
                }

                _uiState.value = _uiState.value.copy(
                    parsedCommand = commandText,
                    saveSuccess = true
                )

                textToSpeechManager.speakMoodConfirmation(
                    command.moodLevel,
                    command.energyLevel,
                    command.painLevel
                )

                Timber.i("Saved mood entry: $entry")
            } catch (e: Exception) {
                Timber.e(e, "Error saving mood entry")
                textToSpeechManager.speakError("Failed to save mood entry")
            }
        }
    }

    private fun saveWinEntry(command: WinCommand) {
        viewModelScope.launch {
            try {
                val entry = WinEntry(
                    description = command.description,
                    category = command.category ?: "personal"
                )

                database.winEntryDao().insert(entry)

                _uiState.value = _uiState.value.copy(
                    parsedCommand = "Win: ${command.description}",
                    saveSuccess = true
                )

                textToSpeechManager.speakWinConfirmation(command.description)

                Timber.i("Saved win entry: $entry")
            } catch (e: Exception) {
                Timber.e(e, "Error saving win entry")
                textToSpeechManager.speakError("Failed to save win")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionManager.reset()
    }
}

data class VoiceInputUiState(
    val recognizedText: String = "",
    val parsedCommand: String? = null,
    val saveSuccess: Boolean = false
)
