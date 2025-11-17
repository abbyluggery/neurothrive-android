package com.neurothrive.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.local.entities.ImposterSyndromeSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImposterSyndromeScreen(
    onNavigateBack: () -> Unit,
    onSave: (ImposterSyndromeSession) -> Unit
) {
    val stepInstructions = listOf(
        "What negative thought are you experiencing?",
        "How strongly do you believe this thought right now? (1=not at all, 10=completely)",
        "What evidence supports this thought being true?",
        "What evidence suggests this thought might not be accurate?",
        "What's an alternative, more balanced perspective?",
        "After examining the evidence, how strongly do you believe the original thought now?"
    )

    var currentStep by remember { mutableStateOf(0) }
    var thoughtText by remember { mutableStateOf("") }
    var believabilityBefore by remember { mutableStateOf(5f) }
    var evidenceFor by remember { mutableStateOf("") }
    var evidenceAgainst by remember { mutableStateOf("") }
    var alternativePerspective by remember { mutableStateOf("") }
    var believabilityAfter by remember { mutableStateOf(5f) }

    val scrollState = rememberScrollState()

    // Validate if current step is complete
    val canProceed = when (currentStep) {
        0 -> thoughtText.isNotBlank()
        1 -> true // Slider always has a value
        2 -> true // Optional field
        3 -> true // Optional field
        4 -> true // Optional field
        5 -> true // Slider always has a value
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FYF Therapy") },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Step indicator
                Text(
                    text = "Step ${currentStep + 1} of ${stepInstructions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Instruction card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = stepInstructions[currentStep],
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Input fields based on step
                when (currentStep) {
                    0 -> {
                        OutlinedTextField(
                            value = thoughtText,
                            onValueChange = { thoughtText = it },
                            label = { Text("Negative Thought") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                    1 -> {
                        Column {
                            Text(
                                text = "Believability: ${believabilityBefore.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Slider(
                                value = believabilityBefore,
                                onValueChange = { believabilityBefore = it },
                                valueRange = 1f..10f,
                                steps = 8,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1 - Not at all", style = MaterialTheme.typography.bodySmall)
                                Text("10 - Completely", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    2 -> {
                        OutlinedTextField(
                            value = evidenceFor,
                            onValueChange = { evidenceFor = it },
                            label = { Text("Evidence FOR (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = { Text("List any facts that support this thought...") }
                        )
                    }
                    3 -> {
                        OutlinedTextField(
                            value = evidenceAgainst,
                            onValueChange = { evidenceAgainst = it },
                            label = { Text("Evidence AGAINST (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = { Text("List any facts that contradict this thought...") }
                        )
                    }
                    4 -> {
                        OutlinedTextField(
                            value = alternativePerspective,
                            onValueChange = { alternativePerspective = it },
                            label = { Text("Alternative Perspective (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = { Text("What's a more balanced way to think about this?...") }
                        )
                    }
                    5 -> {
                        Column {
                            Text(
                                text = "Believability: ${believabilityAfter.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Slider(
                                value = believabilityAfter,
                                onValueChange = { believabilityAfter = it },
                                valueRange = 1f..10f,
                                steps = 8,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1 - Not at all", style = MaterialTheme.typography.bodySmall)
                                Text("10 - Completely", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous button
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Next/Save button
                if (currentStep < stepInstructions.size - 1) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = canProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            val session = ImposterSyndromeSession(
                                thoughtText = thoughtText,
                                believabilityBefore = believabilityBefore.toInt(),
                                evidenceFor = evidenceFor.ifBlank { null },
                                evidenceAgainst = evidenceAgainst.ifBlank { null },
                                alternativePerspective = alternativePerspective.ifBlank { null },
                                believabilityAfter = believabilityAfter.toInt()
                            )
                            onSave(session)
                        },
                        enabled = canProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
