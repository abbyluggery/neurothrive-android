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
import com.neurothrive.assistant.data.local.entities.WinEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinListScreen(
    onNavigateBack: () -> Unit,
    viewModel: WinListViewModel = hiltViewModel()
) {
    val winEntries by viewModel.winEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Win Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Win")
            }
        }
    ) { paddingValues ->
        if (winEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        "No wins",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No wins yet. Tap + to celebrate one!")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(winEntries) { entry ->
                    WinEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteWinEntry(it) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        WinEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { description, category ->
                viewModel.addWinEntry(description, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WinEntryCard(
    entry: WinEntry,
    onDelete: (WinEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        "Win",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.category != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text(entry.category) }
                    )
                }
            }
            IconButton(onClick = { onDelete(entry) }) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun WinEntryDialog(
    onDismiss: () -> Unit,
    onSave: (description: String, category: String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("career", "health", "personal", "other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Win") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(description, selectedCategory) },
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

@HiltViewModel
class WinListViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val _winEntries = MutableStateFlow<List<WinEntry>>(emptyList())
    val winEntries: StateFlow<List<WinEntry>> = _winEntries

    init {
        loadWinEntries()
    }

    private fun loadWinEntries() {
        viewModelScope.launch {
            database.winEntryDao().getAllFlow().collect { entries ->
                _winEntries.value = entries
            }
        }
    }

    fun addWinEntry(description: String, category: String?) {
        viewModelScope.launch {
            val entry = WinEntry(
                description = description,
                category = category
            )
            database.winEntryDao().insert(entry)
        }
    }

    fun deleteWinEntry(entry: WinEntry) {
        viewModelScope.launch {
            database.winEntryDao().delete(entry)
        }
    }
}
