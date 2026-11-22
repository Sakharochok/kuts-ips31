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
import org.junit.Assert.assertNotEquals
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

    /** Стріляє по необстріляних клітинках ворожого поля по черзі, доки не промахнеться. */
    private fun GameViewModel.shootUntilMiss(): Coord {
        for (coord in allCoords()) {
            if (state.value.enemyCells[coord.row][coord.col] != CellState.EMPTY) continue
            onEnemyCellClick(coord)
            val event = state.value.lastEvent as GameEvent.PlayerShot
            if (event.result == ShotResult.Miss) return coord
        }
        error("No miss found")
    }

    @Test
    fun `initial state shows player fleet and hides enemy fleet`() {
        val state = GameViewModel(Random(1)).state.value

        assertEquals(Phase.PLAYER_TURN, state.phase)
        assertTrue(state.canShuffle)
        assertEquals(10, state.playerShipsLeft)
        assertEquals(10, state.enemyShipsLeft)
        assertEquals(20, state.playerCells.flatten().count { it == CellState.SHIP })
        assertTrue(state.enemyCells.flatten().all { it == CellState.EMPTY })
        assertNull(state.lastEvent)
    }

    @Test
    fun `miss lets the computer play until it misses`() = runTest(dispatcher) {
        val vm = GameViewModel(Random(3), computerDelayMs = 10)

        val target = vm.shootUntilMiss()
        assertEquals(Phase.COMPUTER_TURN, vm.state.value.phase)
        assertFalse(vm.state.value.canShuffle)
        assertEquals(CellState.MISS, vm.state.value.enemyCells[target.row][target.col])

        advanceUntilIdle()

        val state = vm.state.value
        val event = state.lastEvent as GameEvent.ComputerShot
        assertEquals(ShotResult.Miss, event.result)
        assertEquals(event.target, state.lastComputerTarget)
        assertEquals(Phase.PLAYER_TURN, state.phase)
    }

    @Test
    fun `clicks are ignored while the computer is playing`() = runTest(dispatcher) {
        val vm = GameViewModel(Random(3), computerDelayMs = 10)
        vm.shootUntilMiss()
        val before = vm.state.value

        vm.onEnemyCellClick(Coord(9, 9))

        assertEquals(before, vm.state.value)
        advanceUntilIdle()
    }

    @Test
    fun `shuffle changes the fleet only before the first shot`() = runTest(dispatcher) {
        val vm = GameViewModel(Random(4), computerDelayMs = 10)
        val initial = vm.state.value.playerCells

        vm.onShuffleFleet()
        val shuffled = vm.state.value.playerCells
        assertNotEquals(initial, shuffled)

        vm.shootUntilMiss()
        advanceUntilIdle()
        val afterShot = vm.state.value
        vm.onShuffleFleet()
        assertEquals(afterShot, vm.state.value)
    }

    @Test
    fun `new game resets the state and cancels the computer turn`() = runTest(dispatcher) {
        val vm = GameViewModel(Random(5), computerDelayMs = 10)
        vm.shootUntilMiss()

        vm.onNewGame()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(Phase.PLAYER_TURN, state.phase)
        assertTrue(state.canShuffle)
        assertNull(state.lastEvent)
        assertTrue(state.enemyCells.flatten().all { it == CellState.EMPTY })
    }

    @Test
    fun `game can be played to the end`() = runTest(dispatcher) {
        val vm = GameViewModel(Random(6), computerDelayMs = 1)
        var guard = 0
        while (vm.state.value.phase != Phase.PLAYER_WON && vm.state.value.phase != Phase.COMPUTER_WON) {
            val target = allCoords().first { vm.state.value.enemyCells[it.row][it.col] == CellState.EMPTY }
            vm.onEnemyCellClick(target)
            advanceUntilIdle()
            check(++guard < 200)
        }

        val state = vm.state.value
        if (state.phase == Phase.PLAYER_WON) {
            assertEquals(0, state.enemyShipsLeft)
        } else {
            assertEquals(0, state.playerShipsLeft)
            // Після поразки гравцю показують, де стояли вцілілі ворожі кораблі.
            assertTrue(state.enemyCells.flatten().contains(CellState.SHIP))
        }
    }
}
