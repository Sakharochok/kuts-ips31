package com.kuts.battleship.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FleetPlacerTest {

    @Test
    fun `places standard fleet following the rules`() {
        repeat(200) { seed ->
            val board = FleetPlacer(Random(seed)).createBoard()

            assertEquals(Board.FLEET.sortedDescending(), board.ships.map { it.size }.sortedDescending())
            board.ships.forEach { ship ->
                assertTrue(ship.cells.all { it.isInside(Board.SIZE) })
                val others = board.ships - ship
                val touching = ship.cells.flatMap { it.neighbors() + it }
                    .any { cell -> others.any { cell in it } }
                assertTrue("Ships touch on seed $seed", !touching)
            }
        }
    }

    @Test
    fun `different seeds give different layouts`() {
        val first = FleetPlacer(Random(1)).createBoard().ships.map { it.cells }.toSet()
        val second = FleetPlacer(Random(2)).createBoard().ships.map { it.cells }.toSet()
        assertTrue(first != second)
    }

    @Test
    fun `same seed gives same layout`() {
        val first = FleetPlacer(Random(7)).createBoard().ships.map { it.cells }
        val second = FleetPlacer(Random(7)).createBoard().ships.map { it.cells }
        assertEquals(first, second)
    }

    @Test
    fun `impossible fleet fails`() {
        assertThrows { FleetPlacer(Random(0)).createBoard(size = 3, fleet = listOf(4)) }
    }
}
