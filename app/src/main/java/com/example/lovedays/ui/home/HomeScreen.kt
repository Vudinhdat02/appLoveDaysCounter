package com.example.lovedays.ui.home

import androidx.compose.animation.AnimatedContent
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
                PersonColumn(state.myAvatarPath, state.myName, Modifier.weight(1f))
                BeatingHeart(size = 44.dp, modifier = Modifier.padding(horizontal = 8.dp))
                PersonColumn(state.partnerAvatarPath, state.partnerName, Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            val unitLabel = unitLabel(state.unit, state.counterValue)
            val description = pluralStringResource(
                R.plurals.a11y_loved_days, state.displayedDays.toInt(), state.displayedDays.toInt(),
            )
            val tapHint = stringResource(R.string.home_tap_to_change_unit)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.home_together_for),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    AnimatedContent(targetState = state.counterValue to unitLabel, label = "counter") { (value, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = value.toString(),
                                fontSize = 72.sp,
                                lineHeight = 80.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Text(
                        text = tapHint,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
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

            state.nextMilestone?.let { next ->
                Spacer(Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
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
