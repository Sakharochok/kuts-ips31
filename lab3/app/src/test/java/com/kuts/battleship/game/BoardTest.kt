package com.kuts.battleship.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    private fun boardWith(vararg ships: Ship) = Board().apply { ships.forEach { place(it) } }

    @Test
    fun `ship must be a straight contiguous line`() {
        Ship(listOf(Coord(0, 0), Coord(0, 1), Coord(0, 2)))
        Ship(listOf(Coord(3, 5), Coord(1, 5), Coord(2, 5)))
        assertThrows { Ship(listOf(Coord(0, 0), Coord(1, 1))) }
        assertThrows { Ship(listOf(Coord(0, 0), Coord(0, 2))) }
        assertThrows { Ship(emptyList()) }
    }

    @Test
    fun `ships cannot touch even diagonally`() {
        val board = boardWith(Ship.of(Coord(2, 2), 3, horizontal = true))

        assertFalse(board.canPlace(Ship.of(Coord(3, 5), 1, true)))  // діагональ
        assertFalse(board.canPlace(Ship.of(Coord(1, 2), 2, true)))  // зверху впритул
        assertFalse(board.canPlace(Ship.of(Coord(2, 4), 1, true)))  // перетин
        assertTrue(board.canPlace(Ship.of(Coord(4, 2), 3, true)))   // через рядок
    }

    @Test
    fun `ships cannot leave the board`() {
        val board = Board()
        assertFalse(board.canPlace(Ship.of(Coord(0, 8), 3, horizontal = true)))
        assertFalse(board.canPlace(Ship.of(Coord(8, 0), 3, horizontal = false)))
        assertTrue(board.canPlace(Ship.of(Coord(0, 7), 3, horizontal = true)))
    }

    @Test
    fun `place rejects invalid ship`() {
        val board = boardWith(Ship.of(Coord(0, 0), 2, true))
        assertThrows { board.place(Ship.of(Coord(1, 1), 1, true)) }
    }

    @Test
    fun `shoot reports miss hit and sunk`() {
        val ship = Ship.of(Coord(5, 5), 2, horizontal = false)
        val board = boardWith(ship)

        assertEquals(ShotResult.Miss, board.shoot(Coord(0, 0)))
        assertEquals(ShotResult.Hit, board.shoot(Coord(5, 5)))
        val sunk = board.shoot(Coord(6, 5))
        assertTrue(sunk is ShotResult.Sunk)
        assertSame(ship, (sunk as ShotResult.Sunk).ship)
        assertTrue(board.allSunk)
    }

    @Test
    fun `repeated shot is reported`() {
        val board = boardWith(Ship.of(Coord(0, 0), 2, true))
        board.shoot(Coord(9, 9))
        board.shoot(Coord(0, 0))

        assertEquals(ShotResult.AlreadyShot, board.shoot(Coord(9, 9)))
        assertEquals(ShotResult.AlreadyShot, board.shoot(Coord(0, 0)))
    }

    @Test
    fun `shot outside the board is rejected`() {
        assertThrows { Board().shoot(Coord(10, 0)) }
        assertThrows { Board().shoot(Coord(0, -1)) }
    }

    @Test
    fun `cells around sunk ship are marked as shot`() {
        val board = boardWith(Ship.of(Coord(4, 4), 1, true))
        board.shoot(Coord(4, 4))

        Coord(4, 4).neighbors().forEach {
            assertTrue("$it should be marked", board.isShot(it))
            assertEquals(CellState.MISS, board.cellState(it, revealShips = false))
        }
        assertFalse(board.isShot(Coord(2, 2)))
    }

    @Test
    fun `marking around corner ship stays inside the board`() {
        val board = boardWith(Ship.of(Coord(0, 0), 1, true))
        assertTrue(board.shoot(Coord(0, 0)) is ShotResult.Sunk)
        assertTrue(board.isShot(Coord(1, 1)))
    }

    @Test
    fun `cell state depends on visibility`() {
        val board = boardWith(Ship.of(Coord(0, 0), 3, horizontal = true))
        board.shoot(Coord(0, 0))
        board.shoot(Coord(5, 5))

        assertEquals(CellState.HIT, board.cellState(Coord(0, 0), revealShips = false))
        assertEquals(CellState.SHIP, board.cellState(Coord(0, 1), revealShips = true))
        assertEquals(CellState.EMPTY, board.cellState(Coord(0, 1), revealShips = false))
        assertEquals(CellState.MISS, board.cellState(Coord(5, 5), revealShips = false))
        assertEquals(CellState.EMPTY, board.cellState(Coord(9, 9), revealShips = true))

        board.shoot(Coord(0, 1))
        board.shoot(Coord(0, 2))
        assertEquals(CellState.SUNK, board.cellState(Coord(0, 1), revealShips = false))
    }

    @Test
    fun `remaining ships and all sunk`() {
        val board = boardWith(Ship.of(Coord(0, 0), 1, true), Ship.of(Coord(5, 5), 1, true))
        assertFalse(Board().allSunk)
        assertEquals(2, board.remainingShips)

        board.shoot(Coord(0, 0))
        assertEquals(1, board.remainingShips)
        assertFalse(board.allSunk)

        board.shoot(Coord(5, 5))
        assertEquals(0, board.remainingShips)
        assertTrue(board.allSunk)
        assertNull(board.shipAt(Coord(9, 9)))
    }
}

internal fun assertThrows(block: () -> Unit) {
    try {
        block()
    } catch (_: IllegalArgumentException) {
        return
    } catch (_: IllegalStateException) {
        return
    }
    throw AssertionError("Expected exception was not thrown")
}
