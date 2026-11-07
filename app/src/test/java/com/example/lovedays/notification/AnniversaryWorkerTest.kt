package com.example.lovedays.notification

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.lovedays.data.db.AppDatabase
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.SpecialDay
import com.example.lovedays.domain.TriggerType
import com.example.lovedays.testutil.FakeClock
import com.example.lovedays.testutil.FakeNotifier
import com.example.lovedays.testutil.FakeWidgetRefresher
import com.example.lovedays.testutil.testSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AnniversaryWorkerTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val clock = FakeClock(LocalDate.of(2025, 4, 10)) // ngày thứ 100 nếu bắt đầu 01/01/2025
    private val notifier = FakeNotifier()
    private val widget = FakeWidgetRefresher()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    private fun buildWorker(runner: AnniversaryReminderRunner): AnniversaryWorker =
        TestListenableWorkerBuilder<AnniversaryWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                    AnniversaryWorker(appContext, workerParameters, runner)
            })
            .build()

    @Test
    fun sendsExpectedNotifications_onlyOnce() = runTest {
        val settings = testSettingsRepository(tmp.root)
        settings.completeOnboarding("A", "B", LocalDate.of(2025, 1, 1), null, null)
        settings.setRemindSevenDaysBefore(true)
        val specials = SpecialDayRepository(db.specialDayDao(), clock)
        // Sinh nhật ngày mai (nhắc trước 1 ngày) và một ngày bị tắt nhắc.
        specials.save(SpecialDay(title = "Sinh nhật", date = LocalDate.of(2000, 4, 11), repeatYearly = true))
        specials.save(SpecialDay(title = "Tắt nhắc", date = LocalDate.of(2020, 4, 10), repeatYearly = true, remindEnabled = false))
        val runner = AnniversaryReminderRunner(settings, specials, db.notificationLogDao(), notifier, widget, clock)

        val result = buildWorker(runner).doWork()
        assertEquals(ListenableWorker.Result.Success::class, result::class)
        val sent = notifier.sent.map { it.milestone.key to it.trigger }.toSet()
        assertEquals(
            setOf(
                "milestone_days_100" to TriggerType.SAME_DAY,
                "special_1" to TriggerType.ONE_DAY_BEFORE,
            ),
            sent,
        )
        assertEquals(1, widget.count)

        // Chạy lại cùng ngày: không gửi trùng.
        buildWorker(runner).doWork()
        assertEquals(2, notifier.sent.size)
        assertEquals(2, widget.count)
    }

    @Test
    fun notificationsDisabled_sendsNothing() = runTest {
        val settings = testSettingsRepository(tmp.root)
        settings.completeOnboarding("A", "B", LocalDate.of(2025, 1, 1), null, null)
        settings.setNotificationsEnabled(false)
        val runner = AnniversaryReminderRunner(
            settings, SpecialDayRepository(db.specialDayDao(), clock), db.notificationLogDao(), notifier, widget, clock,
        )
        buildWorker(runner).doWork()
        assertEquals(0, notifier.sent.size)
        assertEquals(1, widget.count)
    }
}
