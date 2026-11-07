package com.example.lovedays.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.lovedays.MainActivity
import com.example.lovedays.R
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.core.ui.localizedContext
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.DayCounter
import com.example.lovedays.domain.MilestoneGenerator
import com.example.lovedays.domain.SpecialDay
import com.example.lovedays.ui.components.MilestoneText
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.io.File

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun specialDayRepository(): SpecialDayRepository
    fun clock(): ClockProvider
}

/** Widget đếm ngày. Kích thước 2x2 hiển thị số ngày; 4x2 thêm ảnh, tên và mốc sắp tới. */
open class LoveDaysWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val clock = entry.clock()
        val todayFlow = flow {
            while (true) {
                emit(clock.today()); delay(60_000)
            }
        }
        provideContent {
            val settings by entry.settingsRepository().settings.collectAsState(initial = null)
            val specialDays by entry.specialDayRepository().specialDays.collectAsState(initial = emptyList())
            val today by todayFlow.collectAsState(initial = clock.today())
            WidgetContent(settings, specialDays, today)
        }
    }

    companion object {
        val SMALL = DpSize(110.dp, 110.dp)
        val WIDE = DpSize(250.dp, 110.dp)
    }
}

/** Biến thể mặc định 4x2 trong danh sách widget. */
class LoveDaysWideWidget : LoveDaysWidget()

private val White = ColorProvider(Color.White)
private val WhiteSoft = ColorProvider(Color(0xE6FFFFFF))

@Composable
private fun WidgetContent(settings: CoupleSettings?, specialDays: List<SpecialDay>, today: java.time.LocalDate) {
    val context = localizedContext(LocalContext.current)
    val res = context.resources
    val size = LocalSize.current
    val start = settings?.startDate
    val small = size.width < WIDE_THRESHOLD
    // 2x2: luôn là hình vuông cân đối, căn giữa ô widget.
    val side = minOf(size.width, size.height)
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Box(
        modifier = (if (small) GlanceModifier.size(side) else GlanceModifier.fillMaxSize())
            .background(ImageProvider(R.drawable.widget_background))
            .cornerRadius(if (small) side * 0.24f else 24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (settings == null) return@Box
        if (!settings.onboardingCompleted || start == null) {
            Text(
                text = res.getString(R.string.widget_setup),
                style = TextStyle(color = White, fontSize = 14.sp, textAlign = TextAlign.Center),
            )
            return@Box
        }
        val days = DayCounter.displayedDays(start, today, settings.countStartAsDayOne)
        val daysLabel = res.getQuantityString(R.plurals.label_days, days.toInt())
        val description = res.getQuantityString(R.plurals.a11y_loved_days, days.toInt(), days.toInt())
        if (small) {
            val digits = days.toString().length.coerceAtLeast(3)
            val numberSize = (side.value * (if (digits <= 3) 0.30f else 0.30f * 3f / digits)).sp
            Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.fillMaxSize()) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_heart_bg),
                    contentDescription = null,
                    modifier = GlanceModifier.size(side * 0.82f),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.semantics { contentDescription = description },
                ) {
                    Text("❤", style = TextStyle(color = White, fontSize = (side.value * 0.12f).sp))
                    Text(days.toString(), style = TextStyle(color = White, fontSize = numberSize, fontWeight = FontWeight.Bold))
                    Text(daysLabel, style = TextStyle(color = WhiteSoft, fontSize = (side.value * 0.11f).sp))
                }
            }
        } else {
            val next = MilestoneGenerator.nextUpcoming(
                MilestoneGenerator.allMilestones(start, specialDays, today, settings.countStartAsDayOne),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetAvatar(settings.myAvatarPath, settings.myName)
                    Spacer(GlanceModifier.width(12.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.semantics { contentDescription = description },
                    ) {
                        Text("❤", style = TextStyle(color = White, fontSize = 16.sp))
                        Text(days.toString(), style = TextStyle(color = White, fontSize = 32.sp, fontWeight = FontWeight.Bold))
                        Text(daysLabel, style = TextStyle(color = WhiteSoft, fontSize = 12.sp))
                    }
                    Spacer(GlanceModifier.width(12.dp))
                    WidgetAvatar(settings.partnerAvatarPath, settings.partnerName)
                }
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "${settings.myName} ❤ ${settings.partnerName}",
                    maxLines = 1,
                    style = TextStyle(color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                )
                if (next != null) {
                    Text(
                        text = MilestoneText.countdown(res, next),
                        maxLines = 1,
                        style = TextStyle(color = WhiteSoft, fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                }
            }
        }
    }
}
}

private val WIDE_THRESHOLD = 200.dp

@Composable
private fun WidgetAvatar(path: String?, name: String) {
    val bitmap = remember(path) { path?.let { decodeAvatar(it) } }
    val modifier = GlanceModifier.size(48.dp).cornerRadius(24.dp)
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier.background(ColorProvider(Color(0x33FFFFFF))), contentAlignment = Alignment.Center) {
            Text(
                text = name.trim().take(1).uppercase(),
                style = TextStyle(color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

private fun decodeAvatar(path: String): Bitmap? = runCatching {
    if (!File(path).exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= 144) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}.getOrNull()
