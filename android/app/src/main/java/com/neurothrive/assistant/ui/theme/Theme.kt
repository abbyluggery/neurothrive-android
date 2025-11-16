package com.neurothrive.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCCF6F3),
    tertiary = Color(0xFFFF6E40),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    error = Color(0xFFB00020),
    onBackground = Color(0xFF1C1B1E),
    onSurface = Color(0xFF1C1B1E),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF005047),
    tertiary = Color(0xFFFF6E40),
    background = Color(0xFF1C1B1E),
    surface = Color(0xFF1C1B1E),
    error = Color(0xFFCF6679),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun NeuroThriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
