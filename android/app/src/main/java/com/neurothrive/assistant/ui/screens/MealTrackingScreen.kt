package com.neurothrive.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.local.entities.MealEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTrackingScreen(
    onNavigateBack: () -> Unit,
    onSave: (MealEntry) -> Unit,
    meals: List<MealEntry> = emptyList()
) {
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var showAddMealDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMealDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add meal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Meal type tabs
            ScrollableTabRow(
                selectedTabIndex = when (selectedMealType) {
                    "breakfast" -> 0
                    "lunch" -> 1
                    "dinner" -> 2
                    "snack" -> 3
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedMealType == "breakfast",
                    onClick = { selectedMealType = "breakfast" },
                    text = { Text("Breakfast") }
                )
                Tab(
                    selected = selectedMealType == "lunch",
                    onClick = { selectedMealType = "lunch" },
                    text = { Text("Lunch") }
                )
                Tab(
                    selected = selectedMealType == "dinner",
                    onClick = { selectedMealType = "dinner" },
                    text = { Text("Dinner") }
                )
                Tab(
                    selected = selectedMealType == "snack",
                    onClick = { selectedMealType = "snack" },
                    text = { Text("Snack") }
                )
            }

            // Filter meals by selected type
            val filteredMeals = meals.filter { it.mealType.lowercase() == selectedMealType }

            if (filteredMeals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${selectedMealType}s tracked yet.\nTap + to add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMeals) { meal ->
                        MealCard(meal = meal)
                    }
                }
            }
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            mealType = selectedMealType,
            onDismiss = { showAddMealDialog = false },
            onSave = { mealEntry ->
                onSave(mealEntry)
                showAddMealDialog = false
            }
        )
    }
}

@Composable
private fun MealCard(meal: MealEntry) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.mealType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(Date(meal.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (meal.recipeId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "From recipe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMealDialog(
    mealType: String,
    onDismiss: () -> Unit,
    onSave: (MealEntry) -> Unit
) {
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${mealType.replaceFirstChar { it.uppercase() }}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Meal description") },
                    placeholder = { Text("What did you eat?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Placeholder for photo picker
                OutlinedButton(
                    onClick = { /* TODO: Implement photo picker */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Photo (Coming Soon)")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onSave(
                            MealEntry(
                                mealType = mealType,
                                description = description,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = description.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
