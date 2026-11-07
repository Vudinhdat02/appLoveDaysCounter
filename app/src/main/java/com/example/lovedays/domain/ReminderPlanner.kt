package com.example.lovedays.domain

import java.time.LocalDate

enum class TriggerType(val daysBefore: Long) {
    SAME_DAY(0), ONE_DAY_BEFORE(1), SEVEN_DAYS_BEFORE(7)
}

data class ReminderPreferences(
    val notificationsEnabled: Boolean,
    val remindOneDayBefore: Boolean,
    val remindSevenDaysBefore: Boolean,
)

/** Khoá chống gửi trùng: (mốc, loại nhắc, ngày xảy ra). */
data class SentKey(val eventKey: String, val triggerType: TriggerType, val eventDateEpochDay: Long)

data class PlannedReminder(val milestone: Milestone, val trigger: TriggerType) {
    val sentKey: SentKey get() = SentKey(milestone.key, trigger, milestone.date.toEpochDay())
}

/** Hàm thuần quyết định hôm nay cần gửi những thông báo nào. */
object ReminderPlanner {

    fun plan(
        events: List<Milestone>,
        today: LocalDate,
        preferences: ReminderPreferences,
        alreadySent: Set<SentKey>,
    ): List<PlannedReminder> {
        if (!preferences.notificationsEnabled) return emptyList()
        val triggers = buildList {
            add(TriggerType.SAME_DAY)
            if (preferences.remindOneDayBefore) add(TriggerType.ONE_DAY_BEFORE)
            if (preferences.remindSevenDaysBefore) add(TriggerType.SEVEN_DAYS_BEFORE)
        }
        return events
            .filter { it.remindEnabled }
            .flatMap { event ->
                triggers
                    .filter { event.date == today.plusDays(it.daysBefore) }
                    .map { PlannedReminder(event, it) }
            }
            .filter { it.sentKey !in alreadySent }
            .distinctBy { it.sentKey }
    }
}
