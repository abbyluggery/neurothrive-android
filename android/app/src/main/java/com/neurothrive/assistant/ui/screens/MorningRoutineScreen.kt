package com.neurothrive.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.local.entities.DailyRoutine
import com.neurothrive.assistant.utils.TimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningRoutineScreen(
    onNavigateBack: () -> Unit,
    onSave: (DailyRoutine) -> Unit
) {
    var wakeTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var bedTime by remember { mutableStateOf("") }
    var morningMood by remember { mutableStateOf(5) }
    var morningEnergy by remember { mutableStateOf(5) }
    var morningPain by remember { mutableStateOf(1) }
    var journalEntry by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Morning Routine") },
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
            // Header
            Text(
                text = "Good morning! Let's track your morning routine.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // Time Section
            Text(
                text = "Sleep & Wake Times",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            TimePickerField(
                label = "Wake Time",
                value = wakeTime,
                onValueChange = { wakeTime = it },
                placeholder = "HH:mm (e.g., 07:30)"
            )

            TimePickerField(
                label = "Sleep Time (Previous Night)",
                value = sleepTime,
                onValueChange = { sleepTime = it },
                placeholder = "HH:mm (e.g., 23:00)"
            )

            TimePickerField(
                label = "Bed Time (Previous Night)",
                value = bedTime,
                onValueChange = { bedTime = it },
                placeholder = "HH:mm (e.g., 22:30)"
            )

            Divider()

            // Morning Metrics Section
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            SliderWithLabel(
                label = "Morning Mood",
                value = morningMood,
                range = 1..10,
                onValueChange = { morningMood = it }
            )

            SliderWithLabel(
                label = "Morning Energy",
                value = morningEnergy,
                range = 1..10,
                onValueChange = { morningEnergy = it }
            )

            SliderWithLabel(
                label = "Morning Pain",
                value = morningPain,
                range = 1..10,
                onValueChange = { morningPain = it }
            )

            Divider()

            // Journal Entry
            Text(
                text = "Journal Entry (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = journalEntry,
                onValueChange = { journalEntry = it },
                label = { Text("How are you feeling this morning?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                placeholder = { Text("Write your thoughts here...") }
            )

            // Error message
            if (showError) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Save Button
            Button(
                onClick = {
                    // Validate time inputs
                    val wakeTimeValid = wakeTime.isEmpty() || TimeUtils.parseTime(wakeTime) != null
                    val sleepTimeValid = sleepTime.isEmpty() || TimeUtils.parseTime(sleepTime) != null
                    val bedTimeValid = bedTime.isEmpty() || TimeUtils.parseTime(bedTime) != null

                    if (!wakeTimeValid || !sleepTimeValid || !bedTimeValid) {
                        showError = true
                        errorMessage = "Please enter valid times in HH:mm format (e.g., 07:30)"
                        return@Button
                    }

                    // Get start of day timestamp
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfDayTimestamp = calendar.timeInMillis

                    // Create DailyRoutine with morning routine data
                    val dailyRoutine = DailyRoutine(
                        date = startOfDayTimestamp,
                        moodLevel = morningMood,
                        energyLevel = morningEnergy,
                        painLevel = morningPain,
                        wakeTime = wakeTime.takeIf { it.isNotBlank() },
                        sleepTime = sleepTime.takeIf { it.isNotBlank() },
                        bedTime = bedTime.takeIf { it.isNotBlank() },
                        morningMood = morningMood,
                        morningEnergy = morningEnergy,
                        morningPain = morningPain,
                        journalEntry = journalEntry.takeIf { it.isNotBlank() }
                    )

                    onSave(dailyRoutine)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Morning Routine")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        supportingText = {
            Text("Format: HH:mm (24-hour)", style = MaterialTheme.typography.labelSmall)
        }
    )
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
