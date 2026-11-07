package com.example.lovedays.ui.components

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.lovedays.R
import com.example.lovedays.core.ui.theme.HeartRed
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Ảnh đại diện hình tròn, viền trắng; không có ảnh thì hiện chữ cái đầu. */
@Composable
fun Avatar(path: String?, name: String, size: Dp, modifier: Modifier = Modifier) {
    val shape = CircleShape
    val base = modifier
        .size(size)
        .clip(shape)
        .border(BorderStroke(3.dp, Color.White), shape)
    if (path != null && File(path).exists()) {
        AsyncImage(
            model = File(path),
            contentDescription = stringResource(R.string.a11y_avatar_of, name),
            contentScale = ContentScale.Crop,
            modifier = base,
        )
    } else {
        Box(
            modifier = base.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.trim().take(1).uppercase().ifEmpty { "♥" },
                fontSize = (size.value / 2.6f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/** Trái tim "đập" nhẹ; tắt animation khi hệ thống bật giảm chuyển động. */
@Composable
fun BeatingHeart(size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val scale = if (animationsEnabled) {
        val transition = rememberInfiniteTransition(label = "heart")
        val s by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "heartScale",
        )
        s
    } else {
        1f
    }
    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = HeartRed,
        modifier = modifier.size(size).scale(scale),
    )
}

/** Trạng thái rỗng có minh hoạ và lời gợi ý. */
@Composable
fun EmptyState(emoji: String, title: String, hint: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 56.sp)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.utcMillisToLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/** DatePicker; nếu [maxDate] khác null thì không cho chọn ngày sau [maxDate]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoveDatePickerDialog(
    initial: LocalDate?,
    maxDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val selectable = remember(maxDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                maxDate == null || !utcTimeMillis.utcMillisToLocalDate().isAfter(maxDate)

            override fun isSelectableYear(year: Int): Boolean = maxDate == null || year <= maxDate.year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: maxDate)?.toUtcMillis(),
        selectableDates = selectable,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onConfirm(it.utcMillisToLocalDate()) } },
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = state)
    }
}
