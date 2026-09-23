package com.example.autodroid.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autodroid.core.designsystem.AmbientBackground
import com.example.autodroid.core.designsystem.LocalBrand
import com.example.autodroid.core.designsystem.LocalCarDimens
import com.example.autodroid.core.designsystem.VoiceOrb
import com.example.autodroid.core.designsystem.isWideLayout
import com.example.autodroid.core.voice.VoiceState
import com.example.autodroid.domain.model.RadioStation
import com.example.autodroid.domain.model.VehicleState

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val vehicleState by viewModel.vehicleState.collectAsStateWithLifecycle()
    val isDriving by viewModel.isDriving.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val transcript by viewModel.voiceTranscript.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()

    DriveScreen(
        vehicleState = vehicleState,
        isDriving = isDriving,
        voiceState = voiceState,
        transcript = transcript,
        nowPlaying = nowPlaying,
        onVoiceCommand = { viewModel.triggerVoiceCommand(isLongPress = false) },
        onVoiceCommandLong = { viewModel.triggerVoiceCommand(isLongPress = true) },
        onPreviousStation = viewModel::previousStation,
        onNextStation = viewModel::nextStation
    )
}

@Composable
fun DriveScreen(
    vehicleState: VehicleState,
    isDriving: Boolean,
    voiceState: VoiceState,
    transcript: String,
    nowPlaying: RadioStation,
    onVoiceCommand: () -> Unit,
    onVoiceCommandLong: () -> Unit,
    onPreviousStation: () -> Unit,
    onNextStation: () -> Unit
) {
    val brand = LocalBrand.current
    val dimens = LocalCarDimens.current
    val wide = isWideLayout()

    AmbientBackground(
        brand = brand,
        speedKmh = vehicleState.speedKmh,
        focusMode = isDriving
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(dimens.contentPadding)
        ) {
            if (wide) {
                WideDriveLayout(
                    vehicleState = vehicleState,
                    isDriving = isDriving,
                    nowPlaying = nowPlaying,
                    onPreviousStation = onPreviousStation,
                    onNextStation = onNextStation,
                    voiceState = voiceState,
                    onVoiceCommand = onVoiceCommand,
                    onVoiceCommandLong = onVoiceCommandLong
                )
            } else {
                CompactDriveLayout(
                    brandName = brand.displayName,
                    vehicleState = vehicleState,
                    isDriving = isDriving,
                    nowPlaying = nowPlaying,
                    voiceState = voiceState,
                    onPreviousStation = onPreviousStation,
                    onNextStation = onNextStation,
                    onVoiceCommand = onVoiceCommand,
                    onVoiceCommandLong = onVoiceCommandLong
                )
            }

            // Sit above the bottom media + orb row (touchTarget + gap).
            AnimatedVisibility(
                visible = voiceState != VoiceState.IDLE && transcript.isNotBlank(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = dimens.touchTarget + dimens.bentoGap + 24.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = transcript,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WideDriveLayout(
    vehicleState: VehicleState,
    isDriving: Boolean,
    nowPlaying: RadioStation,
    onPreviousStation: () -> Unit,
    onNextStation: () -> Unit,
    voiceState: VoiceState,
    onVoiceCommand: () -> Unit,
    onVoiceCommandLong: () -> Unit
) {
    val dimens = LocalCarDimens.current
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(dimens.bentoGap)
    ) {
        SpeedHeroTile(
            vehicleState = vehicleState,
            isDriving = isDriving,
            modifier = Modifier.weight(1.2f).fillMaxHeight()
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(dimens.bentoGap)
        ) {
            DrivingStatusPill(isDriving = isDriving)
            AnimatedVisibility(visible = !isDriving, enter = fadeIn(), exit = fadeOut()) {
                EnergyTile(
                    fuelPercent = vehicleState.fuelLevelPercent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.energyRingSize * 1.8f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            // Full-width media first — 76dp prev/next + 76dp orb cannot share a half-column.
            NowPlayingTile(
                station = nowPlaying,
                onPrevious = onPreviousStation,
                onNext = onNextStation,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                VoiceOrb(
                    voiceState = voiceState,
                    onClick = onVoiceCommand,
                    onLongClick = onVoiceCommandLong,
                    size = dimens.touchTarget
                )
            }
        }
    }
}

@Composable
private fun CompactDriveLayout(
    brandName: String,
    vehicleState: VehicleState,
    isDriving: Boolean,
    nowPlaying: RadioStation,
    voiceState: VoiceState,
    onPreviousStation: () -> Unit,
    onNextStation: () -> Unit,
    onVoiceCommand: () -> Unit,
    onVoiceCommandLong: () -> Unit
) {
    val dimens = LocalCarDimens.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimens.bentoGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isDriving) {
                Arrangement.End
            } else {
                Arrangement.SpaceBetween
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isDriving) {
                Text(
                    text = brandName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
            }
            DrivingStatusPill(isDriving = isDriving)
        }
        SpeedHeroTile(
            vehicleState = vehicleState,
            isDriving = isDriving,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(max = 320.dp)
                .widthIn(max = 400.dp)
                .align(Alignment.CenterHorizontally)
        )
        AnimatedVisibility(visible = !isDriving) {
            EnergyTile(
                fuelPercent = vehicleState.fuelLevelPercent,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.bentoGap)
        ) {
            NowPlayingTile(
                station = nowPlaying,
                onPrevious = onPreviousStation,
                onNext = onNextStation,
                modifier = Modifier.weight(1f)
            )
            VoiceOrb(
                voiceState = voiceState,
                onClick = onVoiceCommand,
                onLongClick = onVoiceCommandLong,
                size = dimens.touchTarget
            )
        }
    }
}

/** Kept for existing Compose snapshot tests. */
@Composable
fun GaugeCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.size(200.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = unit, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
