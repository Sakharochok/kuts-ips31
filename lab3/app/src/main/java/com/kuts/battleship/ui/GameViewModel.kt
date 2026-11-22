package com.kuts.battleship.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuts.battleship.game.Board
import com.kuts.battleship.game.CellState
import com.kuts.battleship.game.Coord
import com.kuts.battleship.game.Game
import com.kuts.battleship.game.Phase
import com.kuts.battleship.game.ShotResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

/** Остання подія гри; текст для неї підбирає UI з ресурсів поточної мови. */
sealed interface GameEvent {
    data class PlayerShot(val target: Coord, val result: ShotResult) : GameEvent
    data class ComputerShot(val target: Coord, val result: ShotResult) : GameEvent
}

data class GameUiState(
    val playerCells: List<List<CellState>>,
    val enemyCells: List<List<CellState>>,
    val phase: Phase,
    val canShuffle: Boolean,
    val playerShipsLeft: Int,
    val enemyShipsLeft: Int,
    val lastEvent: GameEvent?,
) {
    val lastComputerTarget: Coord? get() = (lastEvent as? GameEvent.ComputerShot)?.target
}

/**
 * Тримає партію між перестворенням Activity (поворот екрана, зміна мови)
 * і програє хід комп'ютера з невеликою паузою, щоб його постріли було видно.
 */
class GameViewModel(
    private val random: Random = Random.Default,
    private val computerDelayMs: Long = COMPUTER_DELAY_MS,
) : ViewModel() {

    private var game = Game(random)
    private var computerJob: Job? = null

    private val _state = MutableStateFlow(snapshot(lastEvent = null))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    fun onEnemyCellClick(target: Coord) {
        if (game.phase != Phase.PLAYER_TURN) return
        val result = game.playerShoot(target)
        publish(GameEvent.PlayerShot(target, result))
        if (game.phase == Phase.COMPUTER_TURN) playComputerTurn()
    }

    fun onNewGame() {
        computerJob?.cancel()
        game = Game(random)
        publish(lastEvent = null)
    }

    fun onShuffleFleet() {
        if (game.isStarted) return
        game.shufflePlayerFleet()
        publish(lastEvent = null)
    }

    private fun playComputerTurn() {
        computerJob = viewModelScope.launch {
            while (game.phase == Phase.COMPUTER_TURN) {
                delay(computerDelayMs)
                val move = game.computerShoot()
                publish(GameEvent.ComputerShot(move.target, move.result))
            }
        }
    }

    private fun publish(lastEvent: GameEvent?) {
        _state.value = snapshot(lastEvent)
        if (game.isOver) Timber.i("Result shown to player: %s", game.phase)
    }

    private fun snapshot(lastEvent: GameEvent?) = GameUiState(
        playerCells = cells(game.playerBoard, revealShips = true),
        enemyCells = cells(game.computerBoard, revealShips = game.isOver),
        phase = game.phase,
        canShuffle = !game.isStarted,
        playerShipsLeft = game.playerBoard.remainingShips,
        enemyShipsLeft = game.computerBoard.remainingShips,
        lastEvent = lastEvent,
    )

    private fun cells(board: Board, revealShips: Boolean) =
        List(board.size) { row -> List(board.size) { col -> board.cellState(Coord(row, col), revealShips) } }

    private companion object {
        const val COMPUTER_DELAY_MS = 700L
    }
}
