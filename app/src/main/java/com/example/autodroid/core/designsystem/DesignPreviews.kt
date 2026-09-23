package com.example.autodroid.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.autodroid.core.voice.VoiceState
import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.model.VehicleState
import com.example.autodroid.feature.dashboard.DriveScreen
import com.example.autodroid.feature.dashboard.DrivingStatusPill
import com.example.autodroid.feature.dashboard.SpeedHeroTile

@Preview(name = "Northwind night wide", widthDp = 900, heightDp = 500, uiMode = 0x20)
@Composable
private fun PreviewNorthwindNight() {
    AutoDroidTheme(brand = Brand.NORTHWIND, darkTheme = true) {
        AmbientBackground(brand = Brand.NORTHWIND, speedKmh = 0f) {
            Column(
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Northwind")
                DrivingStatusPill(isDriving = false)
                SpeedHeroTile(
                    vehicleState = VehicleState(speedKmh = 0f, rpm = 800f, gear = 0),
                    isDriving = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Preview(name = "Harbor day driving wide", widthDp = 900, heightDp = 500)
@Composable
private fun PreviewHarborDayDriving() {
    AutoDroidTheme(brand = Brand.HARBOR, darkTheme = false, focusMode = true) {
        AmbientBackground(brand = Brand.HARBOR, speedKmh = 72f, focusMode = true) {
            Column(
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Harbor")
                DrivingStatusPill(isDriving = true)
                SpeedHeroTile(
                    vehicleState = VehicleState(
                        speedKmh = 72f,
                        rpm = 2100f,
                        gear = 4,
                        fuelLevelPercent = 62f
                    ),
                    isDriving = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Summit phone Drive", widthDp = 411, heightDp = 891, uiMode = 0x20)
@Composable
private fun PreviewSummitPhoneDrive() {
    AutoDroidTheme(brand = Brand.SUMMIT, darkTheme = true, focusMode = false) {
        DriveScreen(
            vehicleState = VehicleState(speedKmh = 0f, fuelLevelPercent = 85f, gear = 0, rpm = 800f),
            isDriving = false,
            voiceState = VoiceState.IDLE,
            transcript = "",
            nowPlaying = FakeRadioRepository.STATIONS.first(),
            onVoiceCommand = {},
            onVoiceCommandLong = {},
            onPreviousStation = {},
            onNextStation = {}
        )
    }
}

@Preview(name = "Summit phone driving", widthDp = 411, heightDp = 891, uiMode = 0x20)
@Composable
private fun PreviewSummitPhoneDriving() {
    AutoDroidTheme(brand = Brand.SUMMIT, darkTheme = true, focusMode = true) {
        DriveScreen(
            vehicleState = VehicleState(speedKmh = 72f, fuelLevelPercent = 62f, gear = 3),
            isDriving = true,
            voiceState = VoiceState.IDLE,
            transcript = "",
            nowPlaying = FakeRadioRepository.STATIONS.first(),
            onVoiceCommand = {},
            onVoiceCommandLong = {},
            onPreviousStation = {},
            onNextStation = {}
        )
    }
}
