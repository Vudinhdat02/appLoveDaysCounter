package com.example.lovedays.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.lovedays.core.ui.theme.BrandPink
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lovedays.R
import com.example.lovedays.domain.CounterUnit
import com.example.lovedays.ui.components.rememberAnimationsEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

/** Hình trái tim co giãn theo kích thước ô. */
val HeartShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w / 2f, h * 0.97f)
    cubicTo(w * -0.05f, h * 0.62f, w * -0.02f, h * 0.12f, w * 0.27f, h * 0.06f)
    cubicTo(w * 0.40f, h * 0.03f, w * 0.48f, h * 0.12f, w / 2f, h * 0.20f)
    cubicTo(w * 0.52f, h * 0.12f, w * 0.60f, h * 0.03f, w * 0.73f, h * 0.06f)
    cubicTo(w * 1.02f, h * 0.12f, w * 1.05f, h * 0.62f, w / 2f, h * 0.97f)
    close()
}

/** Khung trái tim hồng hiển thị số ngày; chạm để đổi đơn vị. */
@Composable
fun CounterCard(state: HomeUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unitLabel = unitLabel(state.unit, state.counterValue)
    val description = pluralStringResource(R.plurals.a11y_loved_days, state.displayedDays.toInt(), state.displayedDays.toInt())
    val tapHint = stringResource(R.string.home_tap_to_change_unit)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.home_together_for),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Box(
            modifier = modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.86f)
                .aspectRatio(1.18f)
                .clip(HeartShape)
                .background(heartBrush())
                .testTag("counter")
                .clickable(onClick = onClick)
                .clearAndSetSemantics {
                    contentDescription = if (state.unit == CounterUnit.DAYS) description else "$description. ${state.counterValue} $unitLabel"
                    role = Role.Button
                    onClick(label = tapHint) { onClick(); true }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.fillMaxSize().padding(start = 56.dp, end = 56.dp, top = 44.dp, bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val shown = rememberCountUp(state.counterValue)
                BasicText(
                    text = shown.toString(),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 64.sp),
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(),
                )
                AnimatedContent(targetState = unitLabel, label = "unit") { label ->
                    Text(text = label, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }
        }
    }
}

/** Số chạy từ giá trị cũ tới giá trị mới (lần đầu chạy từ 0). */
@Composable
private fun rememberCountUp(target: Long): Long {
    val enabled = rememberAnimationsEnabled()
    val anim = remember { Animatable(if (enabled) 0f else target.toFloat()) }
    LaunchedEffect(target) {
        if (enabled) anim.animateTo(target.toFloat(), tween(1400, easing = FastOutSlowInEasing)) else anim.snapTo(target.toFloat())
    }
    return anim.value.roundToLong()
}

/** Gradient hồng cố định cho khung trái tim. */
@Composable
private fun heartBrush(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFFFF8FA8), BrandPink, Color(0xFFE0466E)),
    start = Offset(0f, 0f),
    end = Offset(900f, 900f),
)

@Composable
private fun unitLabel(unit: CounterUnit, value: Long): String {
    val q = value.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return when (unit) {
        CounterUnit.DAYS -> pluralStringResource(R.plurals.label_days, q)
        CounterUnit.WEEKS -> pluralStringResource(R.plurals.label_weeks, q)
        CounterUnit.MONTHS -> pluralStringResource(R.plurals.label_months, q)
        CounterUnit.HOURS -> pluralStringResource(R.plurals.label_hours, q)
    }
}
