package com.kuts.battleship.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuts.battleship.game.Board
import com.kuts.battleship.game.CellState
import com.kuts.battleship.game.Coord
import com.kuts.battleship.game.FleetPlacer
import com.kuts.battleship.game.FleetSetup
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

enum class Screen { MENU, RULES, PLACEMENT, BATTLE }

/** Остання подія бою; текст для неї підбирає UI з ресурсів поточної мови. */
sealed interface GameEvent {
    data class PlayerShot(val target: Coord, val result: ShotResult) : GameEvent
    data class ComputerShot(val target: Coord, val result: ShotResult) : GameEvent
}

data class PlacementUiState(
    val cells: List<List<CellState>>,
    /** Розмір корабля -> скільки таких ще треба поставити, від більшого до меншого. */
    val remaining: Map<Int, Int>,
    val selectedSize: Int?,
    val horizontal: Boolean,
    val isComplete: Boolean,
    val lastPlacementFailed: Boolean,
)

data class BattleUiState(
    val playerCells: List<List<CellState>>,
    val enemyCells: List<List<CellState>>,
    val phase: Phase,
    val playerShipsLeft: Int,
    val enemyShipsLeft: Int,
    val lastEvent: GameEvent?,
) {
    val lastComputerTarget: Coord? get() = (lastEvent as? GameEvent.ComputerShot)?.target
    val isOver: Boolean get() = phase == Phase.PLAYER_WON || phase == Phase.COMPUTER_WON
}

data class UiState(
    val screen: Screen,
    val placement: PlacementUiState,
    val battle: BattleUiState?,
) {
    /** У меню можна повернутися до незавершеної партії. */
    val canContinue: Boolean get() = battle != null && !battle.isOver
}

/**
 * Стан усього застосунку: поточний екран, розстановка флоту і бій.
 * Переживає перестворення Activity (поворот екрана, зміна мови);
 * хід комп'ютера програється з паузою, щоб його постріли було видно.
 */
class GameViewModel(
    private val random: Random = Random.Default,
    private val computerDelayMs: Long = COMPUTER_DELAY_MS,
) : ViewModel() {

    private val placer = FleetPlacer(random)
    private var setup = FleetSetup(placer)
    private var selectedSize: Int? = setup.remaining.firstOrNull()
    private var horizontal = true
    private var lastPlacementFailed = false

    private var game: Game? = null
    private var lastEvent: GameEvent? = null
    private var computerJob: Job? = null

    private var screen = Screen.MENU
    private var screenBeforeRules = Screen.MENU

    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // --- Навігація ---

    fun onNewGame() {
        computerJob?.cancel()
        game = null
        lastEvent = null
        setup = FleetSetup(placer)
        selectedSize = setup.remaining.firstOrNull()
        horizontal = true
        lastPlacementFailed = false
        navigate(Screen.PLACEMENT)
    }

    fun onContinue() {
        if (_state.value.canContinue) navigate(Screen.BATTLE)
    }

    fun onShowRules() {
        if (screen != Screen.RULES) screenBeforeRules = screen
        navigate(Screen.RULES)
    }

    /** @return false, якщо "назад" має обробити система (вихід із застосунку з меню). */
    fun onBack(): Boolean {
        when (screen) {
            Screen.MENU -> return false
            Screen.RULES -> navigate(screenBeforeRules)
            Screen.PLACEMENT, Screen.BATTLE -> navigate(Screen.MENU)
        }
        return true
    }

    fun onMenu() = navigate(Screen.MENU)

    // --- Розстановка ---

    fun onSelectShipSize(size: Int) {
        if (size in setup.remaining) selectedSize = size
        lastPlacementFailed = false
        publish()
    }

    fun onRotate() {
        horizontal = !horizontal
        publish()
    }

    /** Торкання порожньої клітинки ставить вибраний корабель, торкання корабля — прибирає його. */
    fun onPlacementCellClick(coord: Coord) {
        val removed = setup.removeAt(coord)
        if (removed != null) {
            selectedSize = removed.size
            lastPlacementFailed = false
        } else {
            val size = selectedSize ?: return
            lastPlacementFailed = !setup.place(size, coord, horizontal)
            if (!lastPlacementFailed) selectNextSize()
        }
        publish()
    }

    fun onAutoPlace() {
        setup.randomize()
        lastPlacementFailed = false
        selectNextSize()
        publish()
    }

    fun onClearPlacement() {
        setup.clear()
        lastPlacementFailed = false
        selectNextSize()
        publish()
    }

    fun onStartBattle() {
        if (!setup.isComplete) return
        game = Game(random = random, placer = placer, playerBoard = setup.board)
        lastEvent = null
        navigate(Screen.BATTLE)
    }

    // --- Бій ---

    fun onEnemyCellClick(target: Coord) {
        val game = game ?: return
        if (game.phase != Phase.PLAYER_TURN) return
        val result = game.playerShoot(target)
        lastEvent = GameEvent.PlayerShot(target, result)
        publish()
        if (game.phase == Phase.COMPUTER_TURN) playComputerTurn(game)
    }

    private fun playComputerTurn(game: Game) {
        computerJob = viewModelScope.launch {
            while (game.phase == Phase.COMPUTER_TURN) {
                delay(computerDelayMs)
                val move = game.computerShoot()
                lastEvent = GameEvent.ComputerShot(move.target, move.result)
                publish()
            }
        }
    }

    // --- Стан ---

    private fun selectNextSize() {
        val remaining = setup.remaining
        if (selectedSize !in remaining) selectedSize = remaining.firstOrNull()
    }

    private fun navigate(target: Screen) {
        Timber.d("Screen %s -> %s", screen, target)
        screen = target
        publish()
    }

    private fun publish() {
        _state.value = snapshot()
    }

    private fun snapshot(): UiState {
        val game = game
        return UiState(
            screen = screen,
            placement = PlacementUiState(
                cells = cells(setup.board, revealShips = true),
                remaining = setup.remaining.groupingBy { it }.eachCount(),
                selectedSize = selectedSize,
                horizontal = horizontal,
                isComplete = setup.isComplete,
                lastPlacementFailed = lastPlacementFailed,
            ),
            battle = game?.let {
                BattleUiState(
                    playerCells = cells(it.playerBoard, revealShips = true),
                    enemyCells = cells(it.computerBoard, revealShips = it.isOver),
                    phase = it.phase,
                    playerShipsLeft = it.playerBoard.remainingShips,
                    enemyShipsLeft = it.computerBoard.remainingShips,
                    lastEvent = lastEvent,
                )
            },
        )
    }

    private fun cells(board: Board, revealShips: Boolean) =
        List(board.size) { row -> List(board.size) { col -> board.cellState(Coord(row, col), revealShips) } }

    private companion object {
        const val COMPUTER_DELAY_MS = 700L
    }
}
