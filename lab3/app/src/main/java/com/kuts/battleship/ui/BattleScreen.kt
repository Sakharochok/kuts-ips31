package com.kuts.battleship.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.kuts.battleship.R
import com.kuts.battleship.game.Phase
import com.kuts.battleship.game.ShotResult

@Composable
fun BattleScreen(state: BattleUiState, viewModel: GameViewModel, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        // Обидва поля мають уміститися на екрані без прокрутки: у портреті — одне під одним,
        // в альбомній орієнтації — поруч.
        val landscape = maxWidth > maxHeight
        val statusWidth = if (landscape) maxWidth else 420.dp
        val boardWidth: Dp = if (landscape) {
            min((maxWidth - 48.dp) / 2, maxHeight - 150.dp)
        } else {
            min(min(maxWidth - 32.dp, 420.dp), (maxHeight - 200.dp) / 2)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCard(state, viewModel, Modifier.widthIn(max = statusWidth))

            val enemy = @Composable {
                BoardSection(stringResource(R.string.board_enemy), state.enemyShipsLeft, boardWidth) {
                    BoardGrid(
                        cells = state.enemyCells,
                        highlight = (state.lastEvent as? GameEvent.PlayerShot)?.target,
                        onCellClick = viewModel::onEnemyCellClick.takeIf { state.phase == Phase.PLAYER_TURN },
                    )
                }
            }
            val player = @Composable {
                BoardSection(stringResource(R.string.board_player), state.playerShipsLeft, boardWidth) {
                    BoardGrid(cells = state.playerCells, highlight = state.lastComputerTarget)
                }
            }
            if (landscape) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { enemy(); player() }
            } else {
                enemy()
                player()
            }
        }
    }

    if (state.isOver) {
        GameOverDialog(
            playerWon = state.phase == Phase.PLAYER_WON,
            onPlayAgain = viewModel::onNewGame,
            onMenu = viewModel::onMenu,
        )
    }
}

@Composable
private fun StatusCard(state: BattleUiState, viewModel: GameViewModel, modifier: Modifier) {
    Card(modifier.fillMaxWidth()) {
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
            if (state.isOver) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Button(onClick = viewModel::onNewGame) { Text(stringResource(R.string.action_play_again)) }
                    OutlinedButton(onClick = viewModel::onMenu) { Text(stringResource(R.string.action_menu)) }
                }
            }
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
private fun BoardSection(title: String, shipsLeft: Int, width: Dp, board: @Composable () -> Unit) {
    Column(Modifier.width(width), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
private fun GameOverDialog(playerWon: Boolean, onPlayAgain: () -> Unit, onMenu: () -> Unit) {
    // Діалог можна закрити, щоб роздивитися поля; кнопки є і в картці статусу.
    var dismissed by rememberSaveable(playerWon) { mutableStateOf(false) }
    if (dismissed) return
    AlertDialog(
        onDismissRequest = { dismissed = true },
        title = { Text(stringResource(if (playerWon) R.string.dialog_won_title else R.string.dialog_lost_title)) },
        text = { Text(stringResource(if (playerWon) R.string.status_player_won else R.string.status_computer_won)) },
        confirmButton = { TextButton(onClick = onPlayAgain) { Text(stringResource(R.string.action_play_again)) } },
        dismissButton = { TextButton(onClick = onMenu) { Text(stringResource(R.string.action_menu)) } },
    )
}
