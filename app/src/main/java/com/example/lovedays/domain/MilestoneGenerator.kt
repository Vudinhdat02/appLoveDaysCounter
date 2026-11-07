package com.example.lovedays.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Hàm thuần sinh danh sách mốc kỷ niệm. */
object MilestoneGenerator {

    /** Số ngày tính trước kể từ hôm nay khi sinh các mốc sắp tới. */
    const val DEFAULT_HORIZON_DAYS = 730L

    fun dayKey(days: Int) = "milestone_days_$days"
    fun yearKey(years: Int) = "milestone_year_$years"
    fun specialKey(id: Long) = "special_$id"

    /**
     * Các giá trị mốc theo ngày không vượt quá [maxDays]:
     * mỗi 100 ngày tới 1000, sau đó mỗi 500 ngày, và luôn có 365 × n. Sắp xếp tăng dần, không trùng.
     */
    fun dayMilestoneValues(maxDays: Long): List<Int> {
        if (maxDays < 100) return emptyList()
        val values = sortedSetOf<Int>()
        var v = 100
        while (v <= 1000 && v <= maxDays) {
            values += v; v += 100
        }
        v = 1500
        while (v <= maxDays) {
            values += v; v += 500
        }
        v = 365
        while (v <= maxDays) {
            values += v; v += 365
        }
        return values.toList()
    }

    /** Ngày rơi vào mốc [days] ngày. Bật "ngày 1" thì ngày bắt đầu là ngày thứ 1. */
    fun dateOfDayMilestone(startDate: LocalDate, days: Int, countStartAsDayOne: Boolean): LocalDate =
        startDate.plusDays(if (countStartAsDayOne) days - 1L else days.toLong())

    /** Kỷ niệm tròn [years] năm. 29/02 sẽ thành 28/02 ở năm không nhuận. */
    fun dateOfYearMilestone(startDate: LocalDate, years: Int): LocalDate = startDate.plusYears(years.toLong())

    fun autoMilestones(
        startDate: LocalDate,
        today: LocalDate,
        countStartAsDayOne: Boolean,
        horizonDays: Long = DEFAULT_HORIZON_DAYS,
    ): List<Milestone> {
        val until = today.plusDays(horizonDays)
        if (until.isBefore(startDate)) return emptyList()
        val maxDays = ChronoUnit.DAYS.between(startDate, until) + 1
        val result = mutableListOf<Milestone>()
        for (days in dayMilestoneValues(maxDays)) {
            val date = dateOfDayMilestone(startDate, days, countStartAsDayOne)
            if (date.isAfter(until)) continue
            result += build(dayKey(days), "", date, MilestoneType.DAYS, today, value = days)
        }
        var years = 1
        while (true) {
            val date = dateOfYearMilestone(startDate, years)
            if (date.isAfter(until)) break
            result += build(yearKey(years), "", date, MilestoneType.YEARS, today, value = years)
            years++
        }
        return result.sortedWith(ORDER)
    }

    /** Lần xảy ra kế tiếp (tính cả hôm nay) của một ngày đặc biệt. */
    fun nextOccurrence(date: LocalDate, repeatYearly: Boolean, today: LocalDate): LocalDate {
        if (!repeatYearly || !date.isBefore(today)) return date
        val thisYear = date.withYear(today.year)
        return if (thisYear.isBefore(today)) date.withYear(today.year + 1) else thisYear
    }

    fun specialDayMilestones(days: List<SpecialDay>, today: LocalDate): List<Milestone> =
        days.map { day ->
            val date = nextOccurrence(day.date, day.repeatYearly, today)
            build(
                key = specialKey(day.id),
                title = day.title,
                date = date,
                type = MilestoneType.CUSTOM,
                today = today,
                emoji = day.emoji,
                remindEnabled = day.remindEnabled,
                specialDayId = day.id,
                repeatYearly = day.repeatYearly,
            )
        }.sortedWith(ORDER)

    /** Mốc tự động + ngày đặc biệt, sắp xếp theo ngày tăng dần. */
    fun allMilestones(
        startDate: LocalDate?,
        specialDays: List<SpecialDay>,
        today: LocalDate,
        countStartAsDayOne: Boolean,
        horizonDays: Long = DEFAULT_HORIZON_DAYS,
    ): List<Milestone> {
        val auto = startDate?.let { autoMilestones(it, today, countStartAsDayOne, horizonDays) }.orEmpty()
        return (auto + specialDayMilestones(specialDays, today)).sortedWith(ORDER)
    }

    /** Mốc gần nhất chưa qua (có thể là hôm nay). */
    fun nextUpcoming(milestones: List<Milestone>): Milestone? =
        milestones.filter { !it.isPassed }.minWithOrNull(ORDER)

    private val ORDER = compareBy<Milestone>({ it.date }, { it.type.ordinal }, { it.value }, { it.key })

    private fun build(
        key: String,
        title: String,
        date: LocalDate,
        type: MilestoneType,
        today: LocalDate,
        value: Int = 0,
        emoji: String? = null,
        remindEnabled: Boolean = true,
        specialDayId: Long? = null,
        repeatYearly: Boolean = false,
    ) = Milestone(
        key = key,
        title = title,
        date = date,
        type = type,
        isPassed = date.isBefore(today),
        daysRemaining = ChronoUnit.DAYS.between(today, date),
        value = value,
        emoji = emoji,
        remindEnabled = remindEnabled,
        specialDayId = specialDayId,
        repeatYearly = repeatYearly,
    )
}
