package com.example.lovedays.testutil

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.domain.PlannedReminder
import com.example.lovedays.notification.ReminderNotifier
import com.example.lovedays.widget.WidgetRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FakeClock(var date: LocalDate, private val zone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")) : ClockProvider {
    override fun now(): Instant = date.atTime(9, 0).atZone(zone).toInstant()
    override fun zone(): ZoneId = zone
}

class FakeNotifier(var allowed: Boolean = true) : ReminderNotifier {
    val sent = mutableListOf<PlannedReminder>()
    override fun canPost() = allowed
    override fun notify(reminder: PlannedReminder, settings: CoupleSettings) {
        sent += reminder
    }
}

class FakeWidgetRefresher : WidgetRefresher {
    var count = 0
    override suspend fun refresh() {
        count++
    }
}

fun testSettingsRepository(dir: File): SettingsRepository = SettingsRepository(
    PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { File(dir, "test_${System.nanoTime()}.preferences_pb") },
    ),
)
