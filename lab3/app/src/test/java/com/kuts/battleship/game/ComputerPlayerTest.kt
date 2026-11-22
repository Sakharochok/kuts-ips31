package com.kuts.battleship.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ComputerPlayerTest {

    /** Стріляє, доки флот не потоплено; повертає кількість пострілів. */
    private fun playUntilAllSunk(ai: ComputerPlayer, board: Board): Int {
        var shots = 0
        while (!board.allSunk) {
            val target = ai.chooseTarget(board)
            assertFalse("AI repeated shot at $target", board.isShot(target))
            ai.onShotResult(target, board.shoot(target))
            shots++
            check(shots <= 100) { "AI did not finish in 100 shots" }
        }
        return shots
    }

    @Test
    fun `never shoots the same cell twice and sinks the whole fleet`() {
        repeat(100) { seed ->
            val board = FleetPlacer(Random(seed)).createBoard()
            playUntilAllSunk(ComputerPlayer(Random(seed + 1000)), board)
            assertTrue(board.allSunk)
        }
    }

    @Test
    fun `is better than random shooting on average`() {
        val average = (0 until 100).map { seed ->
            playUntilAllSunk(ComputerPlayer(Random(seed)), FleetPlacer(Random(seed + 500)).createBoard())
        }.average()
        // Випадкова стрільба в середньому потребує ~95 пострілів.
        assertTrue("Average shots $average", average < 70)
    }

    @Test
    fun `after a hit shoots at an adjacent cell`() {
        val board = Board().apply { place(Ship.of(Coord(5, 5), 3, horizontal = true)) }
        val ai = ComputerPlayer(Random(1))
        board.shoot(Coord(5, 6)).also { ai.onShotResult(Coord(5, 6), it) }

        val target = ai.chooseTarget(board)

        assertTrue(target in Coord(5, 6).orthogonalNeighbors())
    }

    @Test
    fun `with two hits in a row continues along the line`() {
        val board = Board().apply { place(Ship.of(Coord(5, 4), 4, horizontal = true)) }
        val ai = ComputerPlayer(Random(1))
        listOf(Coord(5, 5), Coord(5, 6)).forEach { ai.onShotResult(it, board.shoot(it)) }

        repeat(10) {
            assertTrue(ai.chooseTarget(board) in setOf(Coord(5, 4), Coord(5, 7)))
        }
    }

    @Test
    fun `vertical line is finished along the column`() {
        val board = Board().apply { place(Ship.of(Coord(0, 3), 3, horizontal = false)) }
        val ai = ComputerPlayer(Random(1))
        listOf(Coord(0, 3), Coord(1, 3)).forEach { ai.onShotResult(it, board.shoot(it)) }

        assertEquals(Coord(2, 3), ai.chooseTarget(board))
    }

    @Test
    fun `hunts on checkerboard while big ships remain`() {
        val board = Board().apply { place(Ship.of(Coord(0, 0), 2, true)) }
        val ai = ComputerPlayer(Random(3), fleet = listOf(2))

        repeat(20) {
            val target = ai.chooseTarget(board)
            assertEquals(0, (target.row + target.col) % 2)
        }
    }

    @Test
    fun `forgets sunk ship and returns to hunting`() {
        val board = Board().apply {
            place(Ship.of(Coord(0, 0), 1, true))
            place(Ship.of(Coord(9, 9), 1, true))
        }
        val ai = ComputerPlayer(Random(2), fleet = listOf(1, 1))
        ai.onShotResult(Coord(0, 0), board.shoot(Coord(0, 0)))

        val target = ai.chooseTarget(board)

        assertFalse(board.isShot(target))
    }

    @Test
    fun `fails when no cells left`() {
        val board = Board(size = 1).apply { shoot(Coord(0, 0)) }
        assertThrows { ComputerPlayer(Random(0)).chooseTarget(board) }
    }
}
