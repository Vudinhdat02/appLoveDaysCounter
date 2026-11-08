package com.example.lovedays.ui.components

import android.content.res.Resources
import com.example.lovedays.R
import com.example.lovedays.domain.Milestone
import com.example.lovedays.domain.MilestoneType
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/** Định dạng văn bản dùng chung cho UI, widget và thông báo. */
object MilestoneText {

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun date(date: LocalDate): String = DATE_FORMAT.format(date)

    fun customTitle(m: Milestone): String = listOfNotNull(m.emoji, m.title).joinToString(" ")

    /** "500 ngày", "Kỷ niệm 2 năm", "🎂 Sinh nhật em". */
    fun label(res: Resources, m: Milestone): String = when (m.type) {
        MilestoneType.DAYS -> res.getQuantityString(R.plurals.milestone_days, m.value, m.value)
        MilestoneType.YEARS -> res.getQuantityString(R.plurals.milestone_years, m.value, m.value)
        MilestoneType.MONTHS -> res.getQuantityString(R.plurals.milestone_months, m.value, m.value)
        MilestoneType.CUSTOM -> customTitle(m)
    }

    /** "Còn 12 ngày nữa là 500 ngày" / "Hôm nay là 500 ngày 🎉" / "Đã qua 3 ngày". */
    fun countdown(res: Resources, m: Milestone): String {
        val label = label(res, m)
        val d = m.daysRemaining
        return when {
            d == 0L -> res.getString(R.string.countdown_today, label)
            d > 0 -> res.getQuantityString(R.plurals.countdown_remaining, d.toInt(), d.toInt(), label)
            else -> res.getQuantityString(R.plurals.countdown_passed, (-d).toInt(), (-d).toInt())
        }
    }

    /** Số ngày còn lại dạng ngắn: "Còn 12 ngày", "Hôm nay". */
    fun shortRemaining(res: Resources, m: Milestone): String {
        val d = m.daysRemaining
        return when {
            d == 0L -> res.getString(R.string.today)
            d > 0 -> res.getQuantityString(R.plurals.remaining_short, d.toInt(), d.toInt())
            else -> res.getQuantityString(R.plurals.countdown_passed, (-d).toInt(), (-d).toInt())
        }
    }

    /** "X năm Y tháng Z ngày" (bỏ phần bằng 0). */
    fun period(res: Resources, p: Period): String {
        val parts = buildList {
            if (p.years > 0) add(res.getQuantityString(R.plurals.period_years, p.years, p.years))
            if (p.months > 0) add(res.getQuantityString(R.plurals.period_months, p.months, p.months))
            if (p.days > 0 || isEmpty()) add(res.getQuantityString(R.plurals.period_days, p.days, p.days))
        }
        return parts.joinToString(" ")
    }
}
