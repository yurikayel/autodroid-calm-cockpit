package com.example.autodroid.feature.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.autodroid.core.designsystem.AutoIcons
import com.example.autodroid.core.designsystem.LocalCarDimens
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.model.VehicleState
import kotlin.math.min

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpeedHeroTile(
    vehicleState: VehicleState,
    isDriving: Boolean,
    modifier: Modifier = Modifier
) {
    val dimens = LocalCarDimens.current
    val speedProgress by animateFloatAsState(
        targetValue = (vehicleState.speedKmh / 120f).coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "speedArc"
    )
    val rpmProgress by animateFloatAsState(
        targetValue = (vehicleState.rpm / 4000f).coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "rpmArc"
    )
    val arcColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val rpmColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier.semantics {
            contentDescription = "Speed ${vehicleState.speedKmh.toInt()} kilometers per hour"
        },
        shape = RoundedCornerShape(dimens.heroRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val strokeWidth = min(size.minDimension * 0.08f, 28f)
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val start = 135f
                val sweepMax = 270f
                drawArc(
                    color = trackColor,
                    startAngle = start,
                    sweepAngle = sweepMax,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = arcColor,
                    startAngle = start,
                    sweepAngle = sweepMax * speedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                if (!isDriving) {
                    val inner = diameter * 0.72f
                    val innerLeft = Offset((size.width - inner) / 2f, (size.height - inner) / 2f)
                    drawArc(
                        color = rpmColor.copy(alpha = 0.85f),
                        startAngle = start,
                        sweepAngle = sweepMax * rpmProgress,
                        useCenter = false,
                        topLeft = innerLeft,
                        size = Size(inner, inner),
                        style = Stroke(width = strokeWidth * 0.45f, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${vehicleState.speedKmh.toInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = gearLabel(vehicleState.gear),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EnergyTile(
    fuelPercent: Float,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val dimens = LocalCarDimens.current
    val progress by animateFloatAsState(
        targetValue = (fuelPercent / 100f).coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "fuel"
    )
    val ring = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.outlineVariant
    val ringSize = dimens.energyRingSize

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.tileRadius),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.targetSpacing)
            ) {
                EnergyRing(
                    progress = progress,
                    fuelPercent = fuelPercent,
                    ring = ring,
                    track = track,
                    size = ringSize
                )
                Column {
                    Text("Energy", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "~${(fuelPercent * 4).toInt()} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(dimens.contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EnergyRing(
                    progress = progress,
                    fuelPercent = fuelPercent,
                    ring = ring,
                    track = track,
                    size = ringSize
                )
                Spacer(modifier = Modifier.height(dimens.bentoGap))
                Text("Energy", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "~${(fuelPercent * 4).toInt()} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnergyRing(
    progress: Float,
    fuelPercent: Float,
    ring: Color,
    track: Color,
    size: Dp
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = this.size.minDimension * 0.12f
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = ring,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${fuelPercent.toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NowPlayingTile(
    station: RadioStation,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalCarDimens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.tileRadius),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = dimens.contentPadding / 2,
                    vertical = dimens.contentPadding / 3
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.targetSpacing)
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(dimens.touchTarget)
            ) {
                Icon(
                    AutoIcons.SkipPrevious,
                    contentDescription = "Previous station",
                    modifier = Modifier.size(dimens.iconPrimary)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Now playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${station.frequency} · ${station.genre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(dimens.touchTarget)
            ) {
                Icon(
                    AutoIcons.SkipNext,
                    contentDescription = "Next station",
                    modifier = Modifier.size(dimens.iconPrimary)
                )
            }
        }
    }
}

@Composable
fun DrivingStatusPill(
    isDriving: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = if (isDriving) RoundedCornerShape(20.dp) else RoundedCornerShape(50)
    Surface(
        modifier = modifier.animateContentSize(),
        shape = shape,
        color = if (isDriving) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    ) {
        Text(
            text = if (isDriving) "Driving · focus" else "Parked",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (isDriving) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}

private fun gearLabel(gear: Int): String = when {
    gear > 0 -> "D$gear"
    gear < 0 -> "R"
    else -> "N"
}
