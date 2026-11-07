package com.example.lovedays.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import com.example.lovedays.ui.components.FloatingHearts
import com.example.lovedays.ui.components.rememberAnimationsEnabled
import kotlin.math.roundToLong
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lovedays.R
import com.example.lovedays.domain.CounterUnit
import com.example.lovedays.ui.components.Avatar
import com.example.lovedays.ui.components.BeatingHeart
import com.example.lovedays.ui.components.MilestoneText

@Composable
fun HomeScreen(contentPadding: PaddingValues, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    state?.let { HomeContent(it, contentPadding, onCounterClick = viewModel::cycleUnit) }
}

@Composable
fun HomeContent(state: HomeUiState, contentPadding: PaddingValues, onCounterClick: () -> Unit) {
    val res = LocalResources.current
    val celebrate = state.nextMilestone?.daysRemaining == 0L
    Box(Modifier.fillMaxSize()) {
    if (celebrate) FloatingHearts(count = 28, falling = true, maxAlpha = 0.8f, seed = 3)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Entrance(delayMillis = 0, fromOffsetX = -80, modifier = Modifier.weight(1f)) {
                    PersonColumn(state.myAvatarPath, state.myName, Modifier.fillMaxWidth())
                }
                Entrance(delayMillis = 250) {
                    BeatingHeart(size = 44.dp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Entrance(delayMillis = 0, fromOffsetX = 80, modifier = Modifier.weight(1f)) {
                    PersonColumn(state.partnerAvatarPath, state.partnerName, Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(28.dp))

            val unitLabel = unitLabel(state.unit, state.counterValue)
            val description = pluralStringResource(
                R.plurals.a11y_loved_days, state.displayedDays.toInt(), state.displayedDays.toInt(),
            )
            val tapHint = stringResource(R.string.home_tap_to_change_unit)
            Entrance(delayMillis = 150, fromOffsetY = 60) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("counter")
                    .clickable(onClick = onCounterClick)
                    .clearAndSetSemantics {
                        contentDescription = if (state.unit == CounterUnit.DAYS) {
                            description
                        } else {
                            "$description. ${state.counterValue} $unitLabel"
                        }
                        role = Role.Button
                        onClick(label = tapHint) { onCounterClick(); true }
                    },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(counterBrush())
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.home_together_for),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    val shown = rememberCountUp(state.counterValue)
                    Text(
                        text = shown.toString(),
                        fontSize = 76.sp,
                        lineHeight = 84.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    AnimatedContent(targetState = unitLabel, label = "unit") { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = tapHint,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            }

            Spacer(Modifier.height(20.dp))
            Entrance(delayMillis = 350, fromOffsetY = 40) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = MilestoneText.period(res, state.period),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_since, MilestoneText.date(state.startDate)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            }
            }

            state.nextMilestone?.let { next ->
                Spacer(Modifier.height(24.dp))
                Entrance(delayMillis = 500, fromOffsetY = 60) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.home_next_milestone),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            MilestoneText.countdown(res, next),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            MilestoneText.date(next.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                }
            }
        }
    }
}
}

@Composable
private fun PersonColumn(path: String?, name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(path = path, name = name, size = 104.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
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
    return Brush.linearGradient(
        colors = listOf(scheme.primary, scheme.tertiary, scheme.primary.copy(alpha = 0.85f)),
        start = Offset(0f, 0f),
        end = Offset(600f + 900f * shift, 900f - 600f * shift),
    )
}

/** Hiện dần + trượt vào khi màn hình mở. */
@Composable
fun Entrance(
    delayMillis: Int,
    fromOffsetX: Int = 0,
    fromOffsetY: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val enabled = rememberAnimationsEnabled()
    val visible = remember { MutableTransitionState(!enabled).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        modifier = modifier,
        enter = fadeIn(tween(600, delayMillis)) +
            slideIn(tween(700, delayMillis, easing = FastOutSlowInEasing)) { IntOffset(fromOffsetX, fromOffsetY) } +
            scaleIn(tween(700, delayMillis), initialScale = 0.9f),
    ) { content() }
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
