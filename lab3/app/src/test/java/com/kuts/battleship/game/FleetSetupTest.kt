package com.kuts.battleship.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FleetSetupTest {

    private fun setup() = FleetSetup(FleetPlacer(Random(1)))

    @Test
    fun `starts empty with the whole fleet remaining`() {
        val setup = setup()
        assertEquals(listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1), setup.remaining)
        assertTrue(setup.board.ships.isEmpty())
        assertFalse(setup.isComplete)
    }

    @Test
    fun `placing a ship removes it from remaining`() {
        val setup = setup()

        assertTrue(setup.place(3, Coord(0, 0), horizontal = true))
        assertEquals(listOf(4, 3, 2, 2, 2, 1, 1, 1, 1), setup.remaining)
        assertEquals(Coord(0, 2), setup.board.ships.single().cells.last())

        assertTrue(setup.place(3, Coord(2, 0), horizontal = false))
        assertFalse("no 3-deck ships left", setup.place(3, Coord(6, 6), horizontal = true))
    }

    @Test
    fun `invalid placement is rejected`() {
        val setup = setup()
        setup.place(4, Coord(0, 0), horizontal = true)

        assertFalse("touches", setup.place(1, Coord(1, 4), horizontal = true))
        assertFalse("outside", setup.place(3, Coord(9, 8), horizontal = true))
        assertFalse("unknown size", setup.place(5, Coord(5, 0), horizontal = true))
        assertEquals(1, setup.board.ships.size)
    }

    @Test
    fun `removing a ship returns it to remaining`() {
        val setup = setup()
        setup.place(4, Coord(5, 5), horizontal = false)

        val removed = setup.removeAt(Coord(7, 5))

        assertEquals(4, removed?.size)
        assertTrue(setup.board.ships.isEmpty())
        assertEquals(4, setup.remaining.first())
        assertNull(setup.removeAt(Coord(0, 0)))
    }

    @Test
    fun `full manual placement completes the fleet`() {
        val setup = setup()
        val placements = listOf(
            Triple(4, Coord(0, 0), true), Triple(3, Coord(2, 0), true), Triple(3, Coord(4, 0), true),
            Triple(2, Coord(6, 0), true), Triple(2, Coord(8, 0), true), Triple(2, Coord(0, 6), true),
            Triple(1, Coord(2, 6), true), Triple(1, Coord(4, 6), true), Triple(1, Coord(6, 6), true),
            Triple(1, Coord(8, 6), true),
        )
        placements.forEach { (size, start, horizontal) -> assertTrue(setup.place(size, start, horizontal)) }

        assertTrue(setup.isComplete)
        assertTrue(setup.remaining.isEmpty())
    }

    @Test
    fun `randomize places full fleet and clear empties the board`() {
        val setup = setup()
        setup.randomize()
        assertTrue(setup.isComplete)

        setup.clear()
        assertTrue(setup.board.ships.isEmpty())
        assertEquals(10, setup.remaining.size)
    }

    @Test
    fun `ships cannot be removed after shooting started`() {
        val board = Board().apply { place(Ship.of(Coord(0, 0), 1, true)) }
        board.shoot(Coord(5, 5))
        assertThrows { board.remove(board.ships.first()) }
    }
}
