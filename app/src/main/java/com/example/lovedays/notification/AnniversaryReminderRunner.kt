package com.example.lovedays.notification

import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.db.NotificationLogDao
import com.example.lovedays.data.db.NotificationLogEntity
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.MilestoneGenerator
import com.example.lovedays.domain.ReminderPlanner
import com.example.lovedays.domain.SentKey
import com.example.lovedays.domain.TriggerType
import com.example.lovedays.widget.WidgetRefresher
import javax.inject.Inject

/** Logic của worker nhắc kỷ niệm hằng ngày. Trả về số thông báo đã gửi. */
class AnniversaryReminderRunner @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val specialDayRepository: SpecialDayRepository,
    private val notificationLogDao: NotificationLogDao,
    private val notifier: ReminderNotifier,
    private val widgetRefresher: WidgetRefresher,
    private val clock: ClockProvider,
) {
    suspend fun run(): Int {
        var sentCount = 0
        val settings = settingsRepository.current()
        val startDate = settings.startDate
        if (settings.onboardingCompleted && startDate != null && settings.notificationsEnabled && notifier.canPost()) {
            val today = clock.today()
            val events = MilestoneGenerator.allMilestones(
                startDate = startDate,
                specialDays = specialDayRepository.getAll(),
                today = today,
                countStartAsDayOne = settings.countStartAsDayOne,
                horizonDays = 30,
            )
            val alreadySent = notificationLogDao.getAll().mapNotNull { log ->
                TriggerType.entries.firstOrNull { it.name == log.triggerType }
                    ?.let { SentKey(log.eventKey, it, log.eventDateEpochDay) }
            }.toSet()
            val plan = ReminderPlanner.plan(events, today, settings.reminderPreferences, alreadySent)
            for (reminder in plan) {
                val key = reminder.sentKey
                val inserted = notificationLogDao.insert(
                    NotificationLogEntity(
                        eventKey = key.eventKey,
                        triggerType = key.triggerType.name,
                        eventDateEpochDay = key.eventDateEpochDay,
                        sentAt = clock.now().toEpochMilli(),
                    ),
                )
                if (inserted != -1L) {
                    notifier.notify(reminder, settings)
                    sentCount++
                }
            }
            notificationLogDao.deleteOlderThan(today.minusDays(60).toEpochDay())
        }
        widgetRefresher.refresh()
        return sentCount
    }
}
