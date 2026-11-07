package com.example.lovedays.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lovedays.widget.WidgetRefresher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Chạy 1 lần/ngày vào giờ nhắc: gửi thông báo kỷ niệm và cập nhật widget. */
@HiltWorker
class AnniversaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val runner: AnniversaryReminderRunner,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val sent = runner.run()
        Result.success(androidx.work.workDataOf(KEY_SENT to sent))
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val KEY_SENT = "sent"
    }
}

/** Chạy ngay sau 0h để widget hiển thị số ngày mới. */
@HiltWorker
class WidgetMidnightWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetRefresher: WidgetRefresher,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        widgetRefresher.refresh()
        return Result.success()
    }
}
