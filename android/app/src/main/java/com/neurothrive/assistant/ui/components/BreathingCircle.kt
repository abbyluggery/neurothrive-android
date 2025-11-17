package com.neurothrive.assistant.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BreathingPhase {
    INHALE,
    HOLD_IN,
    EXHALE,
    HOLD_OUT
}

@Composable
fun BreathingCircle(
    phase: BreathingPhase,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Calculate scale based on phase and progress
    val scale = when (phase) {
        BreathingPhase.INHALE -> 0.5f + (0.5f * progress) // grows from 0.5x to 1.0x
        BreathingPhase.HOLD_IN -> 1.0f // stays at 1.0x
        BreathingPhase.EXHALE -> 1.0f - (0.5f * progress) // shrinks from 1.0x to 0.5x
        BreathingPhase.HOLD_OUT -> 0.5f // stays at 0.5x
    }

    // Define colors for each phase
    val color = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF4CAF50) // Green
        BreathingPhase.HOLD_IN -> Color(0xFFFFB300) // Amber
        BreathingPhase.EXHALE -> Color(0xFF2196F3) // Blue
        BreathingPhase.HOLD_OUT -> Color(0xFFFFB300) // Amber
    }

    // Get phase label text
    val phaseLabel = when (phase) {
        BreathingPhase.INHALE -> "Breathe In"
        BreathingPhase.HOLD_IN -> "Hold"
        BreathingPhase.EXHALE -> "Breathe Out"
        BreathingPhase.HOLD_OUT -> "Hold"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            // Base radius is 40% of the smaller dimension
            val baseRadius = (minOf(canvasWidth, canvasHeight) * 0.4f) * scale

            // Draw filled circle with alpha 0.3
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = baseRadius,
                center = Offset(centerX, centerY)
            )

            // Draw stroke circle with width 8dp
            drawCircle(
                color = color,
                radius = baseRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 8.dp.toPx())
            )
        }

        // Phase label text in center
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
