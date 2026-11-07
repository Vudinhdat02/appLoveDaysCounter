package com.example.lovedays

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.di.ApplicationScope
import com.example.lovedays.notification.NotificationHelper
import com.example.lovedays.notification.ReminderScheduler
import com.example.lovedays.widget.WidgetRefresher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LoveDaysApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var specialDayRepository: SpecialDayRepository
    @Inject lateinit var widgetRefresher: WidgetRefresher

    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    @OptIn(FlowPreview::class)
    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannel()
        appScope.launch {
            val settings = settingsRepository.settings.first()
            scheduler.ensureScheduled(settings.reminderHour, settings.reminderMinute)
            if (settings.onboardingCompleted) {
                scheduler.catchUpIfPastReminderTime(settings.reminderHour, settings.reminderMinute)
            }
        }
        // Dữ liệu thay đổi trong app -> widget cập nhật ngay.
        appScope.launch {
            combine(settingsRepository.settings, specialDayRepository.specialDays) { s, d -> s to d }
                .debounce(300)
                .collect { widgetRefresher.refresh() }
        }
    }
}
