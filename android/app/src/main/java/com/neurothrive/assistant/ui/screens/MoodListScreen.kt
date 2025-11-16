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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.data.local.entities.MoodEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodListScreen(
    onNavigateBack: () -> Unit,
    viewModel: MoodListViewModel = hiltViewModel()
) {
    val moodEntries by viewModel.moodEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<MoodEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Mood")
            }
        }
    ) { paddingValues ->
        if (moodEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No mood entries yet. Tap + to add one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moodEntries) { entry ->
                    MoodEntryCard(
                        entry = entry,
                        onEdit = { entryToEdit = it },
                        onDelete = { viewModel.deleteMoodEntry(it) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        MoodEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { mood, energy, pain, notes ->
                viewModel.addMoodEntry(mood, energy, pain, notes)
                showAddDialog = false
            }
        )
    }

    entryToEdit?.let { entry ->
        MoodEntryDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onSave = { mood, energy, pain, notes ->
                viewModel.updateMoodEntry(entry.copy(
                    moodLevel = mood,
                    energyLevel = energy,
                    painLevel = pain,
                    notes = notes
                ))
                entryToEdit = null
            }
        )
    }
}

@Composable
fun MoodEntryCard(
    entry: MoodEntry,
    onEdit: (MoodEntry) -> Unit,
    onDelete: (MoodEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

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
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelMedium
                )
                Row {
                    IconButton(onClick = { onEdit(entry) }) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(entry) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MoodIndicator("Mood", entry.moodLevel, MaterialTheme.colorScheme.primary)
                MoodIndicator("Energy", entry.energyLevel, MaterialTheme.colorScheme.secondary)
                MoodIndicator("Pain", entry.painLevel, MaterialTheme.colorScheme.tertiary)
            }

            if (!entry.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entry.syncedToSalesforce) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudDone,
                        "Synced",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Synced",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MoodIndicator(label: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun MoodEntryDialog(
    entry: MoodEntry? = null,
    onDismiss: () -> Unit,
    onSave: (mood: Int, energy: Int, pain: Int, notes: String) -> Unit
) {
    var mood by remember { mutableStateOf(entry?.moodLevel ?: 5) }
    var energy by remember { mutableStateOf(entry?.energyLevel ?: 5) }
    var pain by remember { mutableStateOf(entry?.painLevel ?: 1) }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Add Mood Entry" else "Edit Mood Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SliderWithLabel("Mood", mood, 1..10) { mood = it }
                SliderWithLabel("Energy", energy, 1..10) { energy = it }
                SliderWithLabel("Pain", pain, 1..10) { pain = it }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(mood, energy, pain, notes) }) {
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

@Composable
fun SliderWithLabel(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}

@HiltViewModel
class MoodListViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _moodEntries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moodEntries: StateFlow<List<MoodEntry>> = _moodEntries

    init {
        loadMoodEntries()
    }

    private fun loadMoodEntries() {
        viewModelScope.launch {
            database.moodEntryDao().getAllFlow().collect { entries ->
                _moodEntries.value = entries
            }
        }
    }

    fun addMoodEntry(mood: Int, energy: Int, pain: Int, notes: String) {
        viewModelScope.launch {
            val entry = MoodEntry(
                moodLevel = mood,
                energyLevel = energy,
                painLevel = pain,
                notes = notes.takeIf { it.isNotBlank() }
            )
            database.moodEntryDao().insert(entry)
        }
    }

    fun updateMoodEntry(entry: MoodEntry) {
        viewModelScope.launch {
            database.moodEntryDao().update(entry)
        }
    }

    fun deleteMoodEntry(entry: MoodEntry) {
        viewModelScope.launch {
            database.moodEntryDao().delete(entry)
        }
    }
}
