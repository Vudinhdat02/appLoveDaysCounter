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
import com.example.lovedays.data.datastore.CounterShape
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

/** Hình bông hoa 8 cánh mềm. */
val FlowerShape: Shape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(cx, cy)
    val steps = 240
    for (i in 0..steps) {
        val a = 2 * PI * i / steps
        val rr = r * (0.90f + 0.10f * cos(8 * a).toFloat())
        val x = cx + rr * cos(a).toFloat()
        val y = cy + rr * sin(a).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

fun CounterShape.toShape(): Shape = when (this) {
    CounterShape.ROUNDED -> RoundedCornerShape(32.dp)
    CounterShape.CIRCLE -> CircleShape
    CounterShape.HEART -> HeartShape
    CounterShape.FLOWER -> FlowerShape
}

@Composable
fun CounterCard(state: HomeUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = state.counterShape.toShape()
    val unitLabel = unitLabel(state.unit, state.counterValue)
    val description = pluralStringResource(R.plurals.a11y_loved_days, state.displayedDays.toInt(), state.displayedDays.toInt())
    val tapHint = stringResource(R.string.home_tap_to_change_unit)
    val onColor = MaterialTheme.colorScheme.onPrimary
    val square = state.counterShape != CounterShape.ROUNDED

    val boxModifier = if (square) {
        modifier.widthIn(max = 300.dp).fillMaxWidth(0.82f).aspectRatio(1f)
    } else {
        modifier.fillMaxWidth()
    }
    Box(
        modifier = boxModifier
            .then(if (state.counterShape == CounterShape.ROUNDED || state.counterShape == CounterShape.CIRCLE) Modifier.shadow(10.dp, shape, clip = false) else Modifier)
            .clip(shape)
            .background(counterBrush())
            .testTag("counter")
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = if (state.unit == CounterUnit.DAYS) description else "$description. ${state.counterValue} $unitLabel"
                role = Role.Button
                onClick(label = tapHint) { onClick(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        val inner = when (state.counterShape) {
            CounterShape.ROUNDED -> Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp)
            CounterShape.HEART -> Modifier.fillMaxSize().padding(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 80.dp)
            else -> Modifier.fillMaxSize().padding(40.dp)
        }
        Column(inner, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.home_together_for),
                style = MaterialTheme.typography.titleMedium,
                color = onColor.copy(alpha = 0.9f),
                maxLines = 1,
            )
            val shown = rememberCountUp(state.counterValue)
            BasicText(
                text = shown.toString(),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = if (square) 64.sp else 76.sp),
                style = TextStyle(color = onColor, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedContent(targetState = unitLabel, label = "unit") { label ->
                Text(text = label, style = MaterialTheme.typography.headlineSmall, color = onColor)
            }
            if (!square || state.counterShape == CounterShape.CIRCLE) {
                Text(
                    text = tapHint,
                    style = MaterialTheme.typography.labelMedium,
                    color = onColor.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/** Chọn hình dạng ô số ngày ngay trên màn hình chính. */
@Composable
fun ShapePicker(selected: CounterShape, onSelect: (CounterShape) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        CounterShape.entries.forEach { s ->
            val isSelected = s == selected
            val label = stringResource(
                when (s) {
                    CounterShape.ROUNDED -> R.string.shape_rounded
                    CounterShape.CIRCLE -> R.string.shape_circle
                    CounterShape.HEART -> R.string.shape_heart
                    CounterShape.FLOWER -> R.string.shape_flower
                },
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = if (isSelected) 1f else 0.6f))
                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(s) })
                    .clearAndSetSemantics {
                        contentDescription = label
                        role = Role.RadioButton
                    },
                contentAlignment = Alignment.Center,
            ) {
                val previewSize = if (s == CounterShape.ROUNDED) Modifier.size(width = 26.dp, height = 18.dp) else Modifier.size(22.dp)
                Box(
                    previewSize
                        .clip(if (s == CounterShape.ROUNDED) RoundedCornerShape(5.dp) else s.toShape())
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant),
                )
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

@Composable
private fun counterBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    val enabled = rememberAnimationsEnabled()
    val shift = if (enabled) {
        val t = rememberInfiniteTransition(label = "gradient")
        val v by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "shift")
        v
    } else {
        0f
    }
    val mid = lerp(scheme.primary, scheme.tertiary, 0.4f)
    return Brush.linearGradient(
        colors = listOf(scheme.primary, mid, scheme.primary),
        start = Offset(0f, 0f),
        end = Offset(600f + 900f * shift, 900f - 600f * shift),
    )
}

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
