package com.example.lovedays.domain

import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Các hàm thuần để tính số ngày yêu. Không bao giờ gọi `LocalDate.now()` bên trong. */
object DayCounter {

    /** Số ngày giữa ngày bắt đầu và hôm nay (0 nếu là cùng ngày). */
    fun daysBetween(startDate: LocalDate, today: LocalDate): Long =
        ChronoUnit.DAYS.between(startDate, today)

    /** Số ngày hiển thị, có tính tới tuỳ chọn "Tính ngày bắt đầu là ngày 1". Không bao giờ âm. */
    fun displayedDays(startDate: LocalDate, today: LocalDate, countStartAsDayOne: Boolean): Long {
        val days = daysBetween(startDate, today)
        if (days < 0) return 0
        return if (countStartAsDayOne) days + 1 else days
    }

    /** "X năm Y tháng Z ngày". */
    fun period(startDate: LocalDate, today: LocalDate): Period =
        if (today.isBefore(startDate)) Period.ZERO else Period.between(startDate, today)

    /** Giá trị của bộ đếm theo đơn vị người dùng chọn khi chạm vào ô số ngày. */
    fun valueIn(
        unit: CounterUnit,
        startDate: LocalDate,
        today: LocalDate,
        countStartAsDayOne: Boolean,
    ): Long {
        val days = displayedDays(startDate, today, countStartAsDayOne)
        return when (unit) {
            CounterUnit.DAYS -> days
            CounterUnit.WEEKS -> days / 7
            CounterUnit.MONTHS ->
                if (today.isBefore(startDate)) 0 else ChronoUnit.MONTHS.between(startDate, today)
            CounterUnit.HOURS -> days * 24
        }
    }

    /** Ngày hôm nay theo múi giờ cho trước. */
    fun todayIn(instant: Instant, zone: ZoneId): LocalDate = instant.atZone(zone).toLocalDate()
}
