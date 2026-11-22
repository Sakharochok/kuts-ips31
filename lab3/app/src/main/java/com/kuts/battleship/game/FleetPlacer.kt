package com.kuts.battleship.game

import timber.log.Timber
import kotlin.random.Random

/** Випадково розставляє флот на новому полі з дотриманням правил. */
class FleetPlacer(private val random: Random) {

    fun createBoard(size: Int = Board.SIZE, fleet: List<Int> = Board.FLEET): Board {
        repeat(MAX_ATTEMPTS) { attempt ->
            val board = Board(size)
            if (tryPlaceFleet(board, fleet)) {
                Timber.d("Fleet placed on attempt %d", attempt + 1)
                return board
            }
        }
        error("Could not place fleet $fleet on ${size}x$size board")
    }

    private fun tryPlaceFleet(board: Board, fleet: List<Int>): Boolean {
        for (shipSize in fleet.sortedDescending()) {
            val options = allPlacements(board, shipSize)
            if (options.isEmpty()) return false
            board.place(options.random(random))
        }
        return true
    }

    private fun allPlacements(board: Board, shipSize: Int): List<Ship> =
        (0 until board.size).flatMap { row ->
            (0 until board.size).flatMap { col ->
                listOf(true, false).map { Ship.of(Coord(row, col), shipSize, it) }
            }
        }.filter { board.canPlace(it) }

    private companion object {
        const val MAX_ATTEMPTS = 100
    }
}
