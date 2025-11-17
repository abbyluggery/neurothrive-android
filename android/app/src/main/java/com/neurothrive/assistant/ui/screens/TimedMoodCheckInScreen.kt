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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.local.entities.MoodEntry
import com.neurothrive.assistant.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimedMoodCheckInScreen(
    onNavigateBack: () -> Unit,
    onSave: (MoodEntry) -> Unit
) {
    // Auto-detect time of day
    val timeOfDay = remember { TimeUtils.getCurrentTimeOfDay() }
    val timeOfDayLabel = TimeUtils.getTimeOfDayLabel(timeOfDay)
    val timeOfDayEmoji = TimeUtils.getTimeOfDayEmoji(timeOfDay)
    val greeting = TimeUtils.getTimeOfDayGreeting(timeOfDay)

    var moodLevel by remember { mutableStateOf(5) }
    var energyLevel by remember { mutableStateOf(5) }
    var painLevel by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$timeOfDayLabel Check-In") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting Section with Emoji
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeOfDayEmoji,
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Divider()

            // Mood Tracking Section
            Text(
                text = "How are you feeling right now?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            SliderWithLabel(
                label = "Mood",
                value = moodLevel,
                range = 1..10,
                onValueChange = { moodLevel = it }
            )

            SliderWithLabel(
                label = "Energy",
                value = energyLevel,
                range = 1..10,
                onValueChange = { energyLevel = it }
            )

            SliderWithLabel(
                label = "Pain",
                value = painLevel,
                range = 1..10,
                onValueChange = { painLevel = it }
            )

            Divider()

            // Notes Section
            Text(
                text = "Notes (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Any thoughts or observations?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                placeholder = { Text("What's on your mind?") }
            )

            // Save Button
            Button(
                onClick = {
                    val moodEntry = MoodEntry(
                        moodLevel = moodLevel,
                        energyLevel = energyLevel,
                        painLevel = painLevel,
                        timestamp = System.currentTimeMillis(),
                        notes = notes.takeIf { it.isNotBlank() },
                        timeOfDay = timeOfDay.name.lowercase()
                    )
                    onSave(moodEntry)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Check-In")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Int,
    range: IntRange = 1..10,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.count() - 2
        )
    }
}
