package com.example.autodroid.feature.radio

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.autodroid.core.designsystem.AutoDroidTheme
import com.example.autodroid.data.repository.FakeRadioRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        state: RadioUiState,
        onRetry: () -> Unit = {},
        onStationClick: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            AutoDroidTheme {
                RadioScreen(uiState = state, onRetry = onRetry, onStationClick = onStationClick)
            }
        }
    }

    @Test
    fun emptyState_showsMessage() {
        render(RadioUiState.Empty)
        composeRule.onNodeWithText("No stations available.").assertIsDisplayed()
    }

    @Test
    fun errorState_retryInvokesCallback() {
        var retries = 0
        render(RadioUiState.Error("Could not reach the station directory"), onRetry = { retries++ })

        composeRule.onNodeWithText("Could not reach the station directory").assertIsDisplayed()
        composeRule.onNodeWithText("Retry")
            .assertHeightIsAtLeast(56.dp)
            .performClick()

        assertEquals(1, retries)
    }

    @Test
    fun restrictedList_showsFooterAndOpensStation() {
        var clickedId: String? = null
        val stations = FakeRadioRepository.STATIONS.take(2)
        render(RadioUiState.Success(stations, hiddenCount = 6), onStationClick = { clickedId = it })

        composeRule.onNodeWithText("6 more when parked").assertIsDisplayed()
        composeRule.onNodeWithText(stations[1].name)
            .assertHeightIsAtLeast(56.dp)
            .assertWidthIsAtLeast(56.dp)
            .performClick()

        assertEquals(stations[1].id, clickedId)
    }

    @Test
    fun wideLayout_touchTargetsMeetCarMinimum() {
        composeRule.setContent {
            AutoDroidTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.autodroid.core.designsystem.LocalCarDimens provides
                        com.example.autodroid.core.designsystem.CarDimens.Wide
                ) {
                    RadioScreen(
                        uiState = RadioUiState.Error("fail"),
                        onRetry = {},
                        onStationClick = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("Retry").assertHeightIsAtLeast(76.dp)
    }
}
