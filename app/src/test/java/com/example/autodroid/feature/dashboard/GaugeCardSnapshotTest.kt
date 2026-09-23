package com.example.autodroid.feature.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.autodroid.core.designsystem.Brand
import com.example.autodroid.core.designsystem.AutoDroidTheme

@RunWith(RobolectricTestRunner::class)
class GaugeCardSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gaugeCard_DisplaysDataCorrectly() {
        composeTestRule.setContent {
            AutoDroidTheme(brand = Brand.NORTHWIND) {
                GaugeCard(title = "SPEED", value = "120", unit = "km/h")
            }
        }
        
        composeTestRule.onNodeWithText("SPEED").assertIsDisplayed()
        composeTestRule.onNodeWithText("120").assertIsDisplayed()
        composeTestRule.onNodeWithText("km/h").assertIsDisplayed()
    }
}

