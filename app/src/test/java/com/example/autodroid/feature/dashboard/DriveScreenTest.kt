package com.example.autodroid.feature.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.autodroid.core.designsystem.AutoDroidTheme
import com.example.autodroid.core.designsystem.Brand
import com.example.autodroid.core.voice.VoiceState
import com.example.autodroid.data.repository.FakeRadioRepository
import com.example.autodroid.domain.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DriveScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val voiceIdleDescription =
        "Voice assistant, idle. Long press for navigation"

    @Test
    fun focusMode_hidesEnergyTileWhileDriving() {
        composeRule.setContent {
            AutoDroidTheme(brand = Brand.NORTHWIND, darkTheme = true, focusMode = true) {
                DriveScreen(
                    vehicleState = VehicleState(speedKmh = 60f, fuelLevelPercent = 80f, gear = 3),
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

        composeRule.onNodeWithText("Driving · focus").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText("Energy").fetchSemanticsNodes().size)
        composeRule.onNodeWithContentDescription(voiceIdleDescription).assertIsDisplayed()
    }

    @Test
    fun parked_showsEnergyTile() {
        composeRule.setContent {
            AutoDroidTheme(brand = Brand.NORTHWIND, darkTheme = true, focusMode = false) {
                DriveScreen(
                    vehicleState = VehicleState(speedKmh = 0f, fuelLevelPercent = 80f, gear = 0),
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

        composeRule.onNodeWithText("Parked").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Energy").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("80%").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun compactDriving_hidesBrandHeadline() {
        composeRule.setContent {
            AutoDroidTheme(brand = Brand.NORTHWIND, darkTheme = true, focusMode = true) {
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

        composeRule.onNodeWithText("Driving · focus").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText("Northwind").fetchSemanticsNodes().size)
    }
}
