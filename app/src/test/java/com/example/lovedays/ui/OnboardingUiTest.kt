package com.example.lovedays.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lovedays.core.ui.theme.LoveDaysTheme
import com.example.lovedays.ui.onboarding.NamesStep
import com.example.lovedays.ui.onboarding.StartDateStep
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class OnboardingUiTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun continueDisabled_untilBothNamesEntered() {
        var continued = 0
        compose.setContent {
            var my by remember { mutableStateOf("") }
            var partner by remember { mutableStateOf("") }
            LoveDaysTheme {
                NamesStep(my, partner, { my = it }, { partner = it }, onBack = {}, onContinue = { continued++ })
            }
        }
        compose.onNodeWithTag("continue").assertIsNotEnabled()
        compose.onNodeWithTag("my_name").performTextInput("Đạt")
        compose.onNodeWithTag("continue").assertIsNotEnabled()
        compose.onNodeWithTag("partner_name").performTextInput("   ")
        compose.onNodeWithTag("continue").assertIsNotEnabled()
        compose.onNodeWithTag("partner_name").performTextInput("Linh")
        compose.onNodeWithTag("continue").assertIsEnabled().performClick()
        assertEquals(1, continued)
    }

    @Test
    fun continueDisabled_withoutStartDate() {
        compose.setContent {
            LoveDaysTheme {
                StartDateStep(startDate = null, today = LocalDate.of(2025, 1, 1), onDateSelected = {}, onBack = {}, onContinue = {})
            }
        }
        compose.onNodeWithTag("continue").assertIsNotEnabled()
    }

    @Test
    fun continueEnabled_withStartDate() {
        compose.setContent {
            LoveDaysTheme {
                StartDateStep(
                    startDate = LocalDate.of(2024, 12, 1),
                    today = LocalDate.of(2025, 1, 1),
                    onDateSelected = {},
                    onBack = {},
                    onContinue = {},
                )
            }
        }
        compose.onNodeWithTag("continue").assertIsEnabled()
    }
}
