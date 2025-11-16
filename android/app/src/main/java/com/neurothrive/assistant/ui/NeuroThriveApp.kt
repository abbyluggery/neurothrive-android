package com.neurothrive.assistant.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.sync.SyncManager
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroThriveApp(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher for microphone
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            navController.navigate("voice")
        } else {
            Timber.w("Audio permission denied")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NeuroThrive") },
                actions = {
                    if (uiState.isAuthenticated) {
                        IconButton(onClick = { viewModel.syncNow() }) {
                            Icon(Icons.Default.Refresh, "Sync Now")
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, "Logout")
                        }
                    } else {
                        IconButton(onClick = onLoginClick) {
                            Icon(Icons.Default.Login, "Login")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasAudioPermission) {
                        navController.navigate("voice")
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            ) {
                Icon(Icons.Default.Mic, "Voice Input")
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel, onLoginClick = onLoginClick)
            }
            composable("voice") {
                VoiceInputScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auth Status Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Authentication Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 8.dp)
                    ) {
                        Surface(
                            color = if (uiState.isAuthenticated)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        }
                    }
                    Text(
                        text = if (uiState.isAuthenticated)
                            "Connected to Salesforce"
                        else
                            "Not authenticated",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Sync Status Card
        if (uiState.isAuthenticated) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.isSyncing)
                            "Syncing data..."
                        else
                            "Last sync: ${uiState.lastSyncTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Database Stats Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Local Database",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mood Entries: ${uiState.moodEntryCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Win Entries: ${uiState.winEntryCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Job Postings: ${uiState.jobPostingCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Daily Routines: ${uiState.dailyRoutineCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Voice Feature Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Input Available",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the microphone button to log mood, wins, or journal entries using your voice!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Session Status Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Build Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Session 1: Database Layer Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "✓ Session 2: Salesforce OAuth Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "✓ Session 3: Voice Integration Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "⧗ Session 4: UI Polish (Pending)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
