package com.kuts.battleship.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuts.battleship.R
import com.kuts.battleship.game.CellState
import com.kuts.battleship.game.Phase
import com.kuts.battleship.game.ShotResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(state)

            BoardSection(
                title = stringResource(R.string.board_enemy),
                shipsLeft = state.enemyShipsLeft,
            ) {
                BoardGrid(
                    cells = state.enemyCells,
                    onCellClick = viewModel::onEnemyCellClick.takeIf { state.phase == Phase.PLAYER_TURN },
                )
            }

            BoardSection(
                title = stringResource(R.string.board_player),
                shipsLeft = state.playerShipsLeft,
            ) {
                BoardGrid(cells = state.playerCells, highlight = state.lastComputerTarget)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::onNewGame) { Text(stringResource(R.string.action_new_game)) }
                OutlinedButton(onClick = viewModel::onShuffleFleet, enabled = state.canShuffle) {
                    Text(stringResource(R.string.action_shuffle))
                }
            }

            Legend()
            Spacer(Modifier.height(8.dp))
        }
    }

    if (state.phase == Phase.PLAYER_WON || state.phase == Phase.COMPUTER_WON) {
        GameOverDialog(playerWon = state.phase == Phase.PLAYER_WON, onPlayAgain = viewModel::onNewGame)
    }
}

@Composable
private fun StatusCard(state: GameUiState) {
    Card(Modifier.widthIn(max = 360.dp).fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(
                    when (state.phase) {
                        Phase.PLAYER_TURN -> R.string.status_player_turn
                        Phase.COMPUTER_TURN -> R.string.status_computer_turn
                        Phase.PLAYER_WON -> R.string.status_player_won
                        Phase.COMPUTER_WON -> R.string.status_computer_won
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.lastEvent?.let { eventText(it) } ?: stringResource(R.string.event_none),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun eventText(event: GameEvent): String = when (event) {
    is GameEvent.PlayerShot -> when (val result = event.result) {
        ShotResult.Miss -> stringResource(R.string.event_player_miss, coordLabel(event.target))
        ShotResult.Hit -> stringResource(R.string.event_player_hit, coordLabel(event.target))
        is ShotResult.Sunk -> stringResource(R.string.event_player_sunk, result.ship.size)
        ShotResult.AlreadyShot -> stringResource(R.string.event_player_repeat, coordLabel(event.target))
    }
    is GameEvent.ComputerShot -> when (val result = event.result) {
        ShotResult.Miss, ShotResult.AlreadyShot -> stringResource(R.string.event_computer_miss, coordLabel(event.target))
        ShotResult.Hit -> stringResource(R.string.event_computer_hit, coordLabel(event.target))
        is ShotResult.Sunk -> stringResource(R.string.event_computer_sunk, result.ship.size)
    }
}

@Composable
private fun BoardSection(title: String, shipsLeft: Int, board: @Composable () -> Unit) {
    Column(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                pluralStringResource(R.plurals.ships_left, shipsLeft, shipsLeft),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        board()
    }
}

@Composable
private fun Legend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            CellState.SHIP to R.string.legend_ship,
            CellState.HIT to R.string.legend_hit,
            CellState.SUNK to R.string.legend_sunk,
            CellState.MISS to R.string.legend_miss,
        ).forEach { (cell, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Cell(cell, Modifier.size(16.dp))
                Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun GameOverDialog(playerWon: Boolean, onPlayAgain: () -> Unit) {
    var dismissed by remember(playerWon) { mutableStateOf(false) }
    if (dismissed) return
    AlertDialog(
        onDismissRequest = { dismissed = true },
        title = { Text(stringResource(if (playerWon) R.string.dialog_won_title else R.string.dialog_lost_title)) },
        text = { Text(stringResource(if (playerWon) R.string.status_player_won else R.string.status_computer_won)) },
        confirmButton = { TextButton(onClick = onPlayAgain) { Text(stringResource(R.string.action_play_again)) } },
        dismissButton = { TextButton(onClick = { dismissed = true }) { Text(stringResource(R.string.action_close)) } },
    )
}
