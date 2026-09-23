package com.example.autodroid.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Soft brand glow behind content. Color warms with speed when not in focus mode;
 * freezes when [focusMode] is true (driving). Never sits behind critical text —
 * screens place content on opaque tiles above this layer.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AmbientBackground(
    brand: Brand = LocalBrand.current,
    speedKmh: Float = 0f,
    focusMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val base = MaterialTheme.colorScheme.background
    val accentTarget = if (focusMode) {
        brand.ambientAccent(dark).copy(alpha = 0.12f)
    } else {
        val warmth = (speedKmh / 80f).coerceIn(0f, 1f)
        brand.ambientAccent(dark).copy(alpha = 0.10f + warmth * 0.18f)
    }
    val accent by animateColorAsState(
        targetValue = accentTarget,
        animationSpec = if (focusMode) {
            MaterialTheme.motionScheme.defaultEffectsSpec()
        } else {
            MaterialTheme.motionScheme.defaultEffectsSpec()
        },
        label = "ambientAccent"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(base)
            .background(
                Brush.radialGradient(
                    colors = listOf(accent, Color.Transparent),
                    radius = 1200f
                )
            ),
        content = content
    )
}
