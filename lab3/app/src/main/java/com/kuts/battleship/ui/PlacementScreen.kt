package com.kuts.battleship.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuts.battleship.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlacementScreen(state: PlacementUiState, viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HintCard(state)

            // Блок вибору кораблів лишається на місці й після розстановки, щоб поле не "стрибало" під пальцем.
            FlowRow(
                modifier = Modifier.heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.remaining.forEach { (size, count) ->
                    FilterChip(
                        selected = size == state.selectedSize,
                        onClick = { viewModel.onSelectShipSize(size) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ShipPreview(size)
                                Text(" × $count")
                            }
                        },
                    )
                }
            }
            OutlinedButton(
                onClick = viewModel::onRotate,
                enabled = !state.isComplete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        if (state.horizontal) R.string.placement_direction_horizontal
                        else R.string.placement_direction_vertical
                    )
                )
            }

            BoardGrid(cells = state.cells, onCellClick = viewModel::onPlacementCellClick)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = viewModel::onAutoPlace, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_auto_place))
                }
                OutlinedButton(onClick = viewModel::onClearPlacement, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_clear))
                }
            }
            Button(
                onClick = viewModel::onStartBattle,
                enabled = state.isComplete,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(R.string.action_start_battle))
            }
        }
    }
}

@Composable
private fun HintCard(state: PlacementUiState) {
    val error = state.lastPlacementFailed
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
        colors = if (error) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val size = state.selectedSize
            Text(
                text = when {
                    state.isComplete -> stringResource(R.string.placement_done)
                    error -> stringResource(R.string.placement_invalid)
                    size == null -> ""
                    state.horizontal -> stringResource(R.string.placement_hint_horizontal, size)
                    else -> stringResource(R.string.placement_hint_vertical, size)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.placement_remove_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
