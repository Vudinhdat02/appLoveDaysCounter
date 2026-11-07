package com.example.lovedays.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lovedays.core.time.TodayProvider
import com.example.lovedays.core.ui.theme.LoveDaysTheme
import com.example.lovedays.data.db.AppDatabase
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.testutil.FakeClock
import com.example.lovedays.testutil.testSettingsRepository
import com.example.lovedays.ui.home.HomeScreen
import com.example.lovedays.ui.home.HomeViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "vi")
class HomeUiTest {

    @get:Rule val compose = createComposeRule()
    @get:Rule val tmp = TemporaryFolder()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()

    @After
    fun tearDown() = db.close()

    @Test
    fun showsDayCountFromFakeClock_andCyclesUnits() {
        val clock = FakeClock(LocalDate.of(2025, 4, 10))
        val settings = testSettingsRepository(tmp.root)
        runBlocking { settings.completeOnboarding("Đạt", "Linh", LocalDate.of(2025, 1, 1), null, null) }
        val vm = HomeViewModel(settings, SpecialDayRepository(db.specialDayDao(), clock), TodayProvider(clock), SavedStateHandle())

        compose.setContent { LoveDaysTheme { HomeScreen(contentPadding = PaddingValues(), viewModel = vm) } }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("counter").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithTag("counter").assertContentDescriptionEquals("Đã yêu 100 ngày")
        compose.onNodeWithText("3 tháng 9 ngày").assertExists()
        compose.onNodeWithText("Từ ngày 01/01/2025").assertExists()
        compose.onNodeWithText("Hôm nay là 100 ngày 🎉").assertExists()

        // Chạm để đổi đơn vị: ngày -> tuần.
        compose.onNodeWithTag("counter").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("counter").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("counter").assertContentDescriptionEquals("Đã yêu 100 ngày. 14 tuần")
        compose.onNodeWithText("14", useUnmergedTree = true).assertExists()
    }
}
