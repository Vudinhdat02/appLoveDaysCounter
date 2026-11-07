package com.example.lovedays.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lovedays.core.time.ClockProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: ClockProvider,
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** Gọi khi mở app. KEEP: không lên lịch lại nếu đã có (WorkManager tự khôi phục sau khi khởi động lại máy). */
    fun ensureScheduled(hour: Int, minute: Int) {
        enqueueReminder(hour, minute, ExistingPeriodicWorkPolicy.KEEP)
        val midnight = PeriodicWorkRequestBuilder<WidgetMidnightWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(0, 1), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_MIDNIGHT, ExistingPeriodicWorkPolicy.KEEP, midnight)
    }

    /** Gọi khi người dùng đổi giờ nhắc. */
    fun reschedule(hour: Int, minute: Int) {
        enqueueReminder(hour, minute, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE)
    }

    /** Nếu đã qua giờ nhắc hôm nay, chạy bù ngay một lần (bảng log chống gửi trùng). */
    fun catchUpIfPastReminderTime(hour: Int, minute: Int) {
        val now = clock.now().atZone(clock.zone()).toLocalTime()
        if (!now.isBefore(LocalTime.of(hour, minute))) runNow()
    }

    fun runNow() {
        workManager.enqueueUniqueWork(
            WORK_REMINDER_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AnniversaryWorker>().build(),
        )
    }

    private fun enqueueReminder(hour: Int, minute: Int, policy: ExistingPeriodicWorkPolicy) {
        val request = PeriodicWorkRequestBuilder<AnniversaryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_REMINDER, policy, request)
    }

    private fun delayUntil(hour: Int, minute: Int): Long {
        val zone = clock.zone()
        val now = clock.now().atZone(zone)
        var target = now.toLocalDate().atTime(hour, minute).atZone(zone)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }

    companion object {
        const val WORK_REMINDER = "anniversary_reminder"
        const val WORK_REMINDER_NOW = "anniversary_reminder_now"
        const val WORK_MIDNIGHT = "widget_midnight_refresh"
    }
}
