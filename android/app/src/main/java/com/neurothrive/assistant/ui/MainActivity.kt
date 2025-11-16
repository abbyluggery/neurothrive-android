package com.neurothrive.assistant.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.data.local.AppDatabase
import com.neurothrive.assistant.sync.SyncManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val oAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Timber.i("OAuth authentication successful")
            viewModel.checkAuthStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeuroThriveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onLoginClick = {
                            val intent = Intent(this, OAuthActivity::class.java)
                            oAuthLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        text = "⧗ Session 3: Voice Integration (Pending)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "⧗ Session 4: UI Polish (Pending)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val oAuthManager: OAuthManager,
    private val syncManager: SyncManager,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        checkAuthStatus()
        loadDatabaseStats()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val isAuth = oAuthManager.isAuthenticated()
            _uiState.value = _uiState.value.copy(
                isAuthenticated = isAuth
            )
            Timber.d("Auth status checked: $isAuth")
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            syncManager.syncNow()
            // Wait a bit for sync to complete
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncTime = "Just now"
            )
            loadDatabaseStats()
        }
    }

    fun logout() {
        viewModelScope.launch {
            oAuthManager.logout()
            syncManager.cancelSync()
            _uiState.value = _uiState.value.copy(
                isAuthenticated = false
            )
            Timber.i("User logged out")
        }
    }

    private fun loadDatabaseStats() {
        viewModelScope.launch {
            val moodCount = database.moodEntryDao().getEntriesBetween(0, Long.MAX_VALUE).size
            val winCount = database.winEntryDao().getWinsSince(0).size
            val jobCount = database.jobPostingDao().getRecentJobs(1000).size
            val routineCount = database.dailyRoutineDao().getRoutinesBetween(0, Long.MAX_VALUE).size

            _uiState.value = _uiState.value.copy(
                moodEntryCount = moodCount,
                winEntryCount = winCount,
                jobPostingCount = jobCount,
                dailyRoutineCount = routineCount
            )
        }
    }
}

data class HomeUiState(
    val isAuthenticated: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "Never",
    val moodEntryCount: Int = 0,
    val winEntryCount: Int = 0,
    val jobPostingCount: Int = 0,
    val dailyRoutineCount: Int = 0
)

@Composable
fun NeuroThriveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}
