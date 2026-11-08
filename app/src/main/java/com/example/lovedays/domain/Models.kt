package com.example.lovedays.domain

import java.time.LocalDate

enum class MilestoneType { DAYS, YEARS, CUSTOM, MONTHS }

/**
 * Một mốc kỷ niệm (tự động hoặc do người dùng tạo). Không lưu xuống DB, luôn được tính ra.
 *
 * @param value số ngày (DAYS), số tháng (MONTHS) hoặc số năm (YEARS); 0 với CUSTOM.
 * @param title tiêu đề do người dùng đặt (chỉ dùng với CUSTOM). Mốc tự động được đặt tên ở tầng UI
 *   để hỗ trợ đa ngôn ngữ.
 */
data class Milestone(
    val key: String,
    val title: String,
    val date: LocalDate,
    val type: MilestoneType,
    val isPassed: Boolean,
    val daysRemaining: Long,
    val value: Int = 0,
    val emoji: String? = null,
    val remindEnabled: Boolean = true,
    val specialDayId: Long? = null,
    val repeatYearly: Boolean = false,
)

data class SpecialDay(
    val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val repeatYearly: Boolean,
    val emoji: String = DEFAULT_EMOJI,
    val remindEnabled: Boolean = true,
    val createdAt: Long = 0,
) {
    companion object {
        const val DEFAULT_EMOJI = "💖"
    }
}

enum class CounterUnit { DAYS, WEEKS, MONTHS, HOURS;

    fun next(): CounterUnit = entries[(ordinal + 1) % entries.size]
}
