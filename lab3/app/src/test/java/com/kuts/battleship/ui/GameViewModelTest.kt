package com.kuts.battleship.ui

import com.kuts.battleship.game.CellState
import com.kuts.battleship.game.Coord
import com.kuts.battleship.game.Phase
import com.kuts.battleship.game.ShotResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun allCoords() = (0 until 100).map { Coord(it / 10, it % 10) }

    private fun GameViewModel.battle() = checkNotNull(state.value.battle)

    /** Меню -> розстановка (випадкова) -> бій. */
    private fun battleReady(seed: Int): GameViewModel = GameViewModel(Random(seed), computerDelayMs = 10).apply {
        onNewGame()
        onAutoPlace()
        onStartBattle()
    }

    /** Стріляє по необстріляних клітинках ворожого поля по черзі, доки не промахнеться. */
    private fun GameViewModel.shootUntilMiss(): Coord {
        for (coord in allCoords()) {
            if (battle().enemyCells[coord.row][coord.col] != CellState.EMPTY) continue
            onEnemyCellClick(coord)
            val event = battle().lastEvent as GameEvent.PlayerShot
            if (event.result == ShotResult.Miss) return coord
        }
        error("No miss found")
    }

    @Test
    fun `app starts in menu without a game`() {
        val state = GameViewModel(Random(1)).state.value

        assertEquals(Screen.MENU, state.screen)
        assertNull(state.battle)
        assertFalse(state.canContinue)
    }

    @Test
    fun `new game opens empty placement`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        val placement = vm.state.value.placement

        assertEquals(Screen.PLACEMENT, vm.state.value.screen)
        assertEquals(mapOf(4 to 1, 3 to 2, 2 to 3, 1 to 4), placement.remaining)
        assertEquals(4, placement.selectedSize)
        assertTrue(placement.horizontal)
        assertFalse(placement.isComplete)
        assertTrue(placement.cells.flatten().all { it == CellState.EMPTY })
    }

    @Test
    fun `manual placement selects next ship and reports invalid positions`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()

        vm.onPlacementCellClick(Coord(0, 0))
        var placement = vm.state.value.placement
        assertEquals(4, placement.cells[0].count { it == CellState.SHIP })
        assertEquals(3, placement.selectedSize)
        assertFalse(placement.lastPlacementFailed)

        vm.onPlacementCellClick(Coord(1, 0)) // торкається 4-палубного
        placement = vm.state.value.placement
        assertTrue(placement.lastPlacementFailed)
        assertEquals(mapOf(3 to 2, 2 to 3, 1 to 4), placement.remaining)

        vm.onRotate()
        vm.onSelectShipSize(1)
        vm.onPlacementCellClick(Coord(5, 5))
        placement = vm.state.value.placement
        assertFalse(placement.horizontal)
        assertFalse(placement.lastPlacementFailed)
        assertEquals(3, placement.remaining[1])
    }

    @Test
    fun `vertical placement goes down`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        vm.onRotate()
        vm.onPlacementCellClick(Coord(2, 7))

        val cells = vm.state.value.placement.cells
        assertTrue((2..5).all { cells[it][7] == CellState.SHIP })
    }

    @Test
    fun `tapping a placed ship removes it and selects its size`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        vm.onPlacementCellClick(Coord(0, 0))

        vm.onPlacementCellClick(Coord(0, 2))

        val placement = vm.state.value.placement
        assertTrue(placement.cells.flatten().none { it == CellState.SHIP })
        assertEquals(4, placement.selectedSize)
        assertEquals(1, placement.remaining[4])
    }

    @Test
    fun `battle starts only with a complete fleet`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        vm.onStartBattle()
        assertEquals(Screen.PLACEMENT, vm.state.value.screen)

        vm.onAutoPlace()
        assertTrue(vm.state.value.placement.isComplete)
        assertNull(vm.state.value.placement.selectedSize)
        vm.onStartBattle()

        val state = vm.state.value
        assertEquals(Screen.BATTLE, state.screen)
        assertEquals(Phase.PLAYER_TURN, vm.battle().phase)
        assertEquals(20, vm.battle().playerCells.flatten().count { it == CellState.SHIP })
        assertTrue(vm.battle().enemyCells.flatten().all { it == CellState.EMPTY })
    }

    @Test
    fun `clear empties the placement board`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        vm.onAutoPlace()
        vm.onClearPlacement()

        val placement = vm.state.value.placement
        assertFalse(placement.isComplete)
        assertEquals(4, placement.selectedSize)
        assertTrue(placement.cells.flatten().all { it == CellState.EMPTY })
    }

    @Test
    fun `miss lets the computer play until it misses`() = runTest(dispatcher) {
        val vm = battleReady(3)

        val target = vm.shootUntilMiss()
        assertEquals(Phase.COMPUTER_TURN, vm.battle().phase)
        assertEquals(CellState.MISS, vm.battle().enemyCells[target.row][target.col])

        advanceUntilIdle()

        val battle = vm.battle()
        val event = battle.lastEvent as GameEvent.ComputerShot
        assertEquals(ShotResult.Miss, event.result)
        assertEquals(event.target, battle.lastComputerTarget)
        assertEquals(Phase.PLAYER_TURN, battle.phase)
    }

    @Test
    fun `clicks are ignored while the computer is playing`() = runTest(dispatcher) {
        val vm = battleReady(3)
        vm.shootUntilMiss()
        val before = vm.state.value

        vm.onEnemyCellClick(Coord(9, 9))

        assertEquals(before, vm.state.value)
        advanceUntilIdle()
    }

    @Test
    fun `back from battle keeps the game to continue`() = runTest(dispatcher) {
        val vm = battleReady(4)
        vm.shootUntilMiss()
        advanceUntilIdle()
        val battle = vm.battle()

        assertTrue(vm.onBack())
        assertEquals(Screen.MENU, vm.state.value.screen)
        assertTrue(vm.state.value.canContinue)

        vm.onContinue()
        assertEquals(Screen.BATTLE, vm.state.value.screen)
        assertEquals(battle, vm.battle())
    }

    @Test
    fun `rules return to the previous screen`() {
        val vm = GameViewModel(Random(1))
        vm.onNewGame()
        vm.onShowRules()
        assertEquals(Screen.RULES, vm.state.value.screen)

        assertTrue(vm.onBack())
        assertEquals(Screen.PLACEMENT, vm.state.value.screen)

        assertTrue(vm.onBack())
        assertEquals(Screen.MENU, vm.state.value.screen)
        assertFalse("system handles back in menu", vm.onBack())
    }

    @Test
    fun `continue does nothing without a game`() {
        val vm = GameViewModel(Random(1))
        vm.onContinue()
        assertEquals(Screen.MENU, vm.state.value.screen)
    }

    @Test
    fun `new game from battle cancels the computer turn and opens placement`() = runTest(dispatcher) {
        val vm = battleReady(5)
        vm.shootUntilMiss()

        vm.onNewGame()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(Screen.PLACEMENT, state.screen)
        assertNull(state.battle)
        assertTrue(state.placement.cells.flatten().all { it == CellState.EMPTY })
    }

    @Test
    fun `game can be played to the end`() = runTest(dispatcher) {
        val vm = battleReady(6)
        var guard = 0
        while (!vm.battle().isOver) {
            val target = allCoords().first { vm.battle().enemyCells[it.row][it.col] == CellState.EMPTY }
            vm.onEnemyCellClick(target)
            advanceUntilIdle()
            check(++guard < 200)
        }

        val battle = vm.battle()
        assertFalse(vm.state.value.canContinue)
        if (battle.phase == Phase.PLAYER_WON) {
            assertEquals(0, battle.enemyShipsLeft)
        } else {
            assertEquals(0, battle.playerShipsLeft)
            // Після поразки гравцю показують, де стояли вцілілі ворожі кораблі.
            assertTrue(battle.enemyCells.flatten().contains(CellState.SHIP))
        }

        vm.onMenu()
        assertEquals(Screen.MENU, vm.state.value.screen)
        assertNotNull(vm.state.value.battle)
    }
}
