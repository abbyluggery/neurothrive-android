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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neurothrive.assistant.data.repository.InsightData
import com.neurothrive.assistant.data.repository.InsightsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsDashboardScreen(
    insightsRepository: InsightsRepository,
    onNavigateBack: () -> Unit
) {
    var insights by remember { mutableStateOf<List<InsightData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load insights on first composition
    LaunchedEffect(Unit) {
        loadInsights(
            insightsRepository = insightsRepository,
            onLoading = { isLoading = it },
            onSuccess = { insights = it },
            onError = { errorMessage = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                loadInsights(
                                    insightsRepository = insightsRepository,
                                    onLoading = { isLoading = it },
                                    onSuccess = { insights = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh Insights")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingState()
                }
                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage ?: "Unknown error",
                        onRetry = {
                            scope.launch {
                                loadInsights(
                                    insightsRepository = insightsRepository,
                                    onLoading = { isLoading = it },
                                    onSuccess = { insights = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }
                    )
                }
                insights.isEmpty() -> {
                    EmptyState(
                        onGenerateInsights = {
                            scope.launch {
                                loadInsights(
                                    insightsRepository = insightsRepository,
                                    onLoading = { isLoading = it },
                                    onSuccess = { insights = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }
                    )
                }
                else -> {
                    InsightsList(insights = insights)
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Generating AI insights...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error generating insights",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyState(onGenerateInsights: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No insights yet",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Generate AI-powered insights based on your mood, wins, and therapy sessions",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGenerateInsights) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Insights")
        }
    }
}

@Composable
private fun InsightsList(insights: List<InsightData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "AI-Powered Insights",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Generated by Claude AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        items(insights) { insight ->
            InsightCard(insight = insight)
        }
    }
}

@Composable
private fun InsightCard(insight: InsightData) {
    val icon = when (insight.type) {
        "mood" -> Icons.Default.Favorite
        "wins" -> Icons.Default.EmojiEvents
        "therapy" -> Icons.Default.Psychology
        else -> Icons.Default.Lightbulb
    }

    val containerColor = when (insight.type) {
        "mood" -> MaterialTheme.colorScheme.tertiaryContainer
        "wins" -> MaterialTheme.colorScheme.secondaryContainer
        "therapy" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                insight.insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                formatTimestamp(insight.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US)
    return dateFormat.format(Date(timestamp))
}

private suspend fun loadInsights(
    insightsRepository: InsightsRepository,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<InsightData>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    onError("")

    insightsRepository.generateAllInsights()
        .onSuccess { insights ->
            onSuccess(insights)
            onLoading(false)
        }
        .onFailure { error ->
            onError(error.message ?: "Failed to generate insights")
            onLoading(false)
        }
}
