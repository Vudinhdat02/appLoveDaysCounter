package com.example.lovedays.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lovedays.MainActivity
import com.example.lovedays.R
import com.example.lovedays.core.ui.localizedContext
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.domain.MilestoneType
import com.example.lovedays.domain.PlannedReminder
import com.example.lovedays.domain.TriggerType
import com.example.lovedays.ui.components.MilestoneText
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Gửi một thông báo nhắc kỷ niệm. Tách thành interface để test worker với bản giả. */
interface ReminderNotifier {
    fun canPost(): Boolean
    fun notify(reminder: PlannedReminder, settings: CoupleSettings)
}

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderNotifier {

    fun createChannel() {
        val ctx = localizedContext(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            ctx.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = ctx.getString(R.string.notif_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    override fun canPost(): Boolean =
        hasPermission() && NotificationManagerCompat.from(context).areNotificationsEnabled()

    override fun notify(reminder: PlannedReminder, settings: CoupleSettings) {
        if (!canPost()) return
        val ctx = localizedContext(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val title = ctx.getString(R.string.notif_title, settings.myName, settings.partnerName)
        val body = body(ctx, reminder)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_heart)
            .setColor(ContextCompat.getColor(context, R.color.brand_pink))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(reminder.sentKey.hashCode(), notification)
        } catch (_: SecurityException) {
            // Quyền bị thu hồi giữa chừng: bỏ qua.
        }
    }

    companion object {
        const val CHANNEL_ID = "anniversary"

        fun body(ctx: Context, reminder: PlannedReminder): String {
            val m = reminder.milestone
            val custom = MilestoneText.customTitle(m)
            return when (reminder.trigger) {
                TriggerType.SAME_DAY -> when (m.type) {
                    MilestoneType.DAYS -> ctx.getString(R.string.notif_today_days, m.value)
                    MilestoneType.YEARS -> ctx.getString(R.string.notif_today_years, m.value)
                    MilestoneType.MONTHS -> ctx.getString(R.string.notif_today_months, m.value)
                    MilestoneType.CUSTOM -> ctx.getString(R.string.notif_today_custom, custom)
                }
                TriggerType.ONE_DAY_BEFORE -> when (m.type) {
                    MilestoneType.DAYS -> ctx.getString(R.string.notif_tomorrow_days, m.value)
                    MilestoneType.YEARS -> ctx.getString(R.string.notif_tomorrow_years, m.value)
                    MilestoneType.MONTHS -> ctx.getString(R.string.notif_tomorrow_months, m.value)
                    MilestoneType.CUSTOM -> ctx.getString(R.string.notif_tomorrow_custom, custom)
                }
                TriggerType.SEVEN_DAYS_BEFORE -> when (m.type) {
                    MilestoneType.DAYS -> ctx.getString(R.string.notif_week_days, m.value)
                    MilestoneType.YEARS -> ctx.getString(R.string.notif_week_years, m.value)
                    MilestoneType.MONTHS -> ctx.getString(R.string.notif_week_months, m.value)
                    MilestoneType.CUSTOM -> ctx.getString(R.string.notif_week_custom, custom)
                }
            }
        }
    }
}
