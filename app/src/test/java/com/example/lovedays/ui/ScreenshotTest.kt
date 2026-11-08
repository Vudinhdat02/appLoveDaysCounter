package com.example.lovedays.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import com.example.lovedays.ui.components.LoveBackground
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lovedays.core.time.TodayProvider
import com.example.lovedays.core.ui.theme.LoveDaysTheme
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.db.AppDatabase
import com.example.lovedays.data.image.ImageStorage
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.SpecialDay
import com.example.lovedays.notification.ReminderScheduler
import com.example.lovedays.testutil.FakeClock
import com.example.lovedays.testutil.testSettingsRepository
import com.example.lovedays.ui.home.HomeScreen
import com.example.lovedays.ui.home.HomeViewModel
import com.example.lovedays.ui.milestones.MilestonesScreen
import com.example.lovedays.ui.milestones.MilestonesViewModel
import com.example.lovedays.ui.onboarding.WelcomeStep
import com.example.lovedays.ui.onboarding.StartDateStep
import com.example.lovedays.data.datastore.AccentColor
import com.example.lovedays.data.datastore.CounterShape
import com.example.lovedays.data.datastore.ThemeMode
import com.example.lovedays.ui.settings.SettingsScreen
import com.example.lovedays.ui.settings.SettingsViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

/** Chụp ảnh màn hình cho README (lưu vào app/build/screenshots). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "vi-w393dp-h852dp-xxhdpi")
class ScreenshotTest {

    @get:Rule val compose = createComposeRule()
    @get:Rule val tmp = TemporaryFolder()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    private val clock = FakeClock(LocalDate.of(2026, 11, 7))
    private lateinit var settings: SettingsRepository
    private lateinit var specials: SpecialDayRepository

    @Before
    fun setUp() = runBlocking {
        settings = testSettingsRepository(tmp.root)
        settings.completeOnboarding("Đạt", "Linh", LocalDate.of(2025, 2, 14), null, null)
        specials = SpecialDayRepository(db.specialDayDao(), clock)
        specials.save(SpecialDay(title = "Sinh nhật Linh", date = LocalDate.of(2001, 12, 2), repeatYearly = true, emoji = "🎂"))
        specials.save(SpecialDay(title = "Lần đầu gặp nhau", date = LocalDate.of(2025, 1, 20), repeatYearly = true, emoji = "🌹"))
        Unit
    }

    @After
    fun tearDown() = db.close()

    private fun shoot(name: String, dark: Boolean = false, accent: AccentColor = AccentColor.PINK, content: @Composable () -> Unit) {
        compose.setContent {
            LoveDaysTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, accentColor = accent) { LoveBackground { content() } }
        }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(2_500)
        compose.waitForIdle()
        Thread.sleep(300)
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun welcome() = shoot("1_welcome") { WelcomeStep(onStart = {}) }

    @Test
    fun home() {
        val vm = HomeViewModel(settings, specials, TodayProvider(clock), SavedStateHandle())
        shoot("2_home") { HomeScreen(PaddingValues(), vm) }
    }

    @Test
    fun homeHeartWhiteAccent() {
        runBlocking { settings.setCounterShape(CounterShape.HEART) }
        val vm = HomeViewModel(settings, specials, TodayProvider(clock), SavedStateHandle())
        shoot("8_home_heart_white", accent = AccentColor.WHITE) { HomeScreen(PaddingValues(), vm) }
    }

    @Test
    fun homeHeartDark() {
        runBlocking { settings.setCounterShape(CounterShape.HEART) }
        val vm = HomeViewModel(settings, specials, TodayProvider(clock), SavedStateHandle())
        shoot("5_home_heart_dark", dark = true) { HomeScreen(PaddingValues(), vm) }
    }

    @Test
    fun homeFlowerWhite() {
        runBlocking { settings.setCounterShape(CounterShape.FLOWER) }
        val vm = HomeViewModel(settings, specials, TodayProvider(clock), SavedStateHandle())
        shoot("6_home_flower_white", accent = AccentColor.WHITE) { HomeScreen(PaddingValues(), vm) }
    }

    @Test
    fun notificationStepDark() = shoot("7_onboarding_dark", dark = true) {
        StartDateStep(startDate = null, today = LocalDate.of(2026, 11, 7), onDateSelected = {}, onBack = {}, onContinue = {})
    }

    @Test
    fun milestones() {
        val vm = MilestonesViewModel(settings, specials, TodayProvider(clock), SavedStateHandle())
        shoot("3_milestones") { MilestonesScreen(PaddingValues(), onAdd = {}, onEdit = {}, viewModel = vm) }
    }

    @Test
    fun settings() {
        val vm = SettingsViewModel(
            settings, specials, db.notificationLogDao(), ImageStorage(context), ReminderScheduler(context, clock), clock,
        )
        shoot("4_settings") { SettingsScreen(PaddingValues(), vm) }
    }
}
