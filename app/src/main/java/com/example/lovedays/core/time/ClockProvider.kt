package com.example.lovedays.core.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Nguồn thời gian, tách riêng để có thể thay bằng đồng hồ giả trong test. */
interface ClockProvider {
    fun now(): Instant
    fun zone(): ZoneId
    fun today(): LocalDate = now().atZone(zone()).toLocalDate()
}

class SystemClockProvider @Inject constructor() : ClockProvider {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

/**
 * Giữ "hôm nay" dưới dạng StateFlow. MainActivity gọi [refresh] khi quay lại app, khi qua 0h
 * và khi nhận ACTION_DATE_CHANGED / TIME_CHANGED / TIMEZONE_CHANGED, nhờ đó số ngày tự cập nhật.
 */
@Singleton
class TodayProvider @Inject constructor(private val clock: ClockProvider) {
    private val _today = MutableStateFlow(clock.today())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    fun refresh() {
        _today.value = clock.today()
    }

    /** Số mili giây còn lại tới 0h ngày mai theo múi giờ hiện tại. */
    fun millisUntilNextDay(): Long {
        val zone = clock.zone()
        val next = clock.today().plusDays(1).atStartOfDay(zone).toInstant()
        return (next.toEpochMilli() - clock.now().toEpochMilli()).coerceAtLeast(1_000L)
    }
}
