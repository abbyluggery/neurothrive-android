package com.neurothrive.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurothrive.assistant.ui.components.BreathingCircle
import com.neurothrive.assistant.ui.components.BreathingPhase
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxBreathingScreen(
    onNavigateBack: () -> Unit
) {
    // Breathing pattern: 4-7-8 (Inhale 4s, Hold 7s, Exhale 8s, Hold 4s = total 23s)
    val phaseDurations = mapOf(
        BreathingPhase.INHALE to 4000L,  // 4 seconds
        BreathingPhase.HOLD_IN to 7000L,  // 7 seconds
        BreathingPhase.EXHALE to 8000L,   // 8 seconds
        BreathingPhase.HOLD_OUT to 4000L  // 4 seconds
    )

    val phases = listOf(
        BreathingPhase.INHALE,
        BreathingPhase.HOLD_IN,
        BreathingPhase.EXHALE,
        BreathingPhase.HOLD_OUT
    )

    var isRunning by remember { mutableStateOf(false) }
    var currentPhaseIndex by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var cycleCount by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }

    val currentPhase = phases[currentPhaseIndex]
    val currentPhaseDuration = phaseDurations[currentPhase] ?: 4000L
    val maxCycles = 5

    // Get phase instruction text
    val phaseInstruction = when (currentPhase) {
        BreathingPhase.INHALE -> "Breathe In"
        BreathingPhase.HOLD_IN -> "Hold Your Breath"
        BreathingPhase.EXHALE -> "Breathe Out"
        BreathingPhase.HOLD_OUT -> "Hold Your Breath"
    }

    // Calculate seconds remaining in current phase
    val secondsRemaining = ((currentPhaseDuration - elapsedTime) / 1000).toInt() + 1

    // Animation logic
    LaunchedEffect(isRunning, currentPhaseIndex, elapsedTime) {
        if (isRunning) {
            delay(100)
            val newElapsedTime = elapsedTime + 100

            if (newElapsedTime >= currentPhaseDuration) {
                // Move to next phase
                val nextPhaseIndex = (currentPhaseIndex + 1) % phases.size

                // If we completed a full cycle
                if (nextPhaseIndex == 0) {
                    val newCycleCount = cycleCount + 1
                    if (newCycleCount >= maxCycles) {
                        // Stop after 5 cycles
                        isRunning = false
                        currentPhaseIndex = 0
                        progress = 0f
                        cycleCount = 0
                        elapsedTime = 0L
                    } else {
                        cycleCount = newCycleCount
                        currentPhaseIndex = nextPhaseIndex
                        elapsedTime = 0L
                        progress = 0f
                    }
                } else {
                    currentPhaseIndex = nextPhaseIndex
                    elapsedTime = 0L
                    progress = 0f
                }
            } else {
                elapsedTime = newElapsedTime
                progress = newElapsedTime.toFloat() / currentPhaseDuration.toFloat()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Box Breathing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cycle counter
            Text(
                text = "Cycle ${cycleCount + 1} of $maxCycles",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Breathing circle (takes most of screen)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                BreathingCircle(
                    phase = currentPhase,
                    progress = progress,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Phase instruction and timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = phaseInstruction,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$secondsRemaining seconds",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Start/Stop button
            Button(
                onClick = {
                    if (isRunning) {
                        // Stop and reset
                        isRunning = false
                        currentPhaseIndex = 0
                        progress = 0f
                        cycleCount = 0
                        elapsedTime = 0L
                    } else {
                        // Start
                        isRunning = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isRunning) "Stop" else "Start",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
