package com.example.lovedays.ui.milestones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lovedays.R
import com.example.lovedays.domain.Milestone
import com.example.lovedays.domain.MilestoneType
import com.example.lovedays.ui.components.EmptyState
import com.example.lovedays.ui.components.MilestoneText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonesScreen(
    contentPadding: PaddingValues,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MilestonesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.milestones_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.special_add))
            }
        },
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MilestoneFilter.entries.forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = {
                            Text(
                                stringResource(
                                    when (f) {
                                        MilestoneFilter.ALL -> R.string.filter_all
                                        MilestoneFilter.AUTO -> R.string.filter_auto
                                        MilestoneFilter.MINE -> R.string.filter_mine
                                    },
                                ),
                            )
                        },
                    )
                }
            }
            if (state.loaded && state.items.isEmpty()) {
                EmptyState(
                    emoji = "💝",
                    title = stringResource(R.string.special_empty_title),
                    hint = stringResource(R.string.special_empty_hint),
                    modifier = Modifier.padding(top = 32.dp),
                )
            } else {
                MilestoneList(state, onEdit)
            }
        }
    }
}

@Composable
private fun MilestoneList(state: MilestonesUiState, onEdit: (Long) -> Unit) {
    val listState = rememberLazyListState()
    var scrolledFor by remember { mutableStateOf<Pair<MilestoneFilter, Boolean>?>(null) }
    // Lần đầu mở (và khi đổi bộ lọc) cuộn tới mốc sắp tới gần nhất.
    LaunchedEffect(state.filter, state.loaded) {
        val key = state.filter to state.loaded
        if (state.loaded && scrolledFor != key) {
            val index = state.items.indexOfFirst { it.key == state.nextKey }
            if (index > 0) listState.scrollToItem((index - 1).coerceAtLeast(0))
            scrolledFor = key
        }
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.items, key = { it.key + "_" + it.date }) { m ->
            MilestoneRow(
                m = m,
                isNext = m.key == state.nextKey,
                onClick = m.specialDayId?.let { id -> { onEdit(id) } },
            )
        }
    }
}

@Composable
fun MilestoneRow(m: Milestone, isNext: Boolean, onClick: (() -> Unit)?) {
    val res = LocalResources.current
    val passedLabel = stringResource(R.string.milestone_passed)
    val nextLabel = stringResource(R.string.milestone_next)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (m.isPassed) 0.55f else 1f)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                stateDescription = when {
                    m.isPassed -> passedLabel
                    isNext -> nextLabel
                    else -> ""
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                when {
                    m.type == MilestoneType.CUSTOM -> Text(m.emoji ?: "💖", fontSize = 26.sp)
                    m.type == MilestoneType.YEARS -> Icon(Icons.Filled.Cake, null, tint = MaterialTheme.colorScheme.primary)
                    else -> Icon(Icons.Filled.Favorite, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = if (m.type == MilestoneType.CUSTOM) m.title else MilestoneText.label(res, m),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        MilestoneText.date(m.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (m.repeatYearly) {
                        Icon(
                            Icons.Filled.Repeat,
                            contentDescription = stringResource(R.string.special_repeat_yearly),
                            modifier = Modifier.padding(start = 6.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (isNext) {
                    Text(
                        nextLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (m.isPassed) {
                Icon(Icons.Filled.CheckCircle, contentDescription = passedLabel, tint = MaterialTheme.colorScheme.primary)
            } else {
                Text(
                    MilestoneText.shortRemaining(res, m),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
