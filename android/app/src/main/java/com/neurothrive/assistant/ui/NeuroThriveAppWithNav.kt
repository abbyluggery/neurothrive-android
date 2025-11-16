package com.neurothrive.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neurothrive.assistant.ui.screens.*
import timber.log.Timber

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Mood : Screen("mood", "Mood", Icons.Default.Favorite)
    object Wins : Screen("wins", "Wins", Icons.Default.EmojiEvents)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Voice : Screen("voice", "Voice", Icons.Default.Mic)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroThriveAppWithNav(
    viewModel: MainViewModel,
    onLoginClick: () -> Unit,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

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
            navController.navigate(Screen.Voice.route)
        } else {
            Timber.w("Audio permission denied")
        }
    }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Mood,
        Screen.Wins,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasAudioPermission) {
                        navController.navigate(Screen.Voice.route)
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel, onLoginClick = onLoginClick)
            }
            composable(Screen.Mood.route) {
                MoodListScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(Screen.Wins.route) {
                WinListScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onThemeChange = onThemeChange
                )
            }
            composable(Screen.Voice.route) {
                VoiceInputScreen(onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}
