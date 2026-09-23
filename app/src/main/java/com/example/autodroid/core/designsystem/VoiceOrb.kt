package com.example.autodroid.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.autodroid.core.voice.VoiceState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun VoiceOrb(
    voiceState: VoiceState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = CarDimens.TouchTarget
) {
    val focusMode = LocalFocusMode.current
    val shapes = remember {
        listOf(
            RoundedPolygon(numVertices = 8, rounding = CornerRounding(1f)),
            RoundedPolygon(numVertices = 8, rounding = CornerRounding(0.35f)),
            RoundedPolygon(numVertices = 9, rounding = CornerRounding(0.4f)),
            RoundedPolygon(numVertices = 12, rounding = CornerRounding(0.2f))
        )
    }
    val targetIndex = when (voiceState) {
        VoiceState.IDLE -> 0
        VoiceState.LISTENING -> 1
        VoiceState.PROCESSING -> 2
        VoiceState.SPEAKING -> 3
    }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val progress = remember { Animatable(0f) }
    val fromIndex = remember { Animatable(0f) }

    LaunchedEffect(targetIndex) {
        val start = fromIndex.value.toInt().coerceIn(0, shapes.lastIndex)
        fromIndex.snapTo(start.toFloat())
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = spatialSpec)
        fromIndex.snapTo(targetIndex.toFloat())
    }

    val shouldPulse = !focusMode &&
        (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING)
    val pulse = if (shouldPulse) {
        val transition = rememberInfiniteTransition(label = "voicePulse")
        transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else {
        1f
    }

    val color = when (voiceState) {
        VoiceState.IDLE -> MaterialTheme.colorScheme.secondary
        VoiceState.LISTENING -> MaterialTheme.colorScheme.tertiary
        VoiceState.PROCESSING -> MaterialTheme.colorScheme.primary
        VoiceState.SPEAKING -> MaterialTheme.colorScheme.secondary
    }
    val onColor = contentColorFor(color)

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription =
                    "Voice assistant, ${voiceState.name.lowercase()}. Long press for navigation"
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val start = fromIndex.value.toInt().coerceIn(0, shapes.lastIndex)
            val end = targetIndex.coerceIn(0, shapes.lastIndex)
            val morph = Morph(shapes[start], shapes[end])
            val path = morph.toPath(progress.value).asComposePath()
            val matrix = Matrix().apply {
                translate(this@Canvas.size.width / 2f, this@Canvas.size.height / 2f)
                val scale = this@Canvas.size.minDimension / 2f * pulse
                scale(scale, scale)
            }
            path.transform(matrix)
            drawPath(path, color = color, style = Fill)
            val mid = Offset(this.size.width / 2f, this.size.height / 2f)
            val w = this.size.minDimension
            drawCircle(color = onColor.copy(alpha = 0.92f), radius = w * 0.08f, center = mid)
        }
    }
}
