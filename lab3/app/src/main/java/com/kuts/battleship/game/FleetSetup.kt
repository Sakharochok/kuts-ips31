package com.kuts.battleship.game

import timber.log.Timber

/**
 * Розстановка флоту гравцем перед боєм: кораблі ставляться по одному,
 * поставлений корабель можна прибрати, а весь флот — розставити випадково.
 */
class FleetSetup(
    private val placer: FleetPlacer,
    private val fleet: List<Int> = Board.FLEET,
    private val size: Int = Board.SIZE,
) {
    var board: Board = Board(size)
        private set

    /** Розміри кораблів, які ще треба поставити, від найбільшого до найменшого. */
    val remaining: List<Int>
        get() {
            val left = fleet.sortedDescending().toMutableList()
            board.ships.forEach { left.remove(it.size) }
            return left
        }

    val isComplete: Boolean get() = remaining.isEmpty()

    /** @return false, якщо такого корабля вже не лишилося або він не вміщується за правилами. */
    fun place(shipSize: Int, start: Coord, horizontal: Boolean): Boolean {
        if (shipSize !in remaining) return false
        val ship = Ship.of(start, shipSize, horizontal)
        if (!board.canPlace(ship)) {
            Timber.d("Cannot place %d-deck ship at %s", shipSize, start)
            return false
        }
        board.place(ship)
        Timber.d("Placed %s", ship)
        return true
    }

    /** Прибирає корабель, що стоїть у клітинці; повертає його або null. */
    fun removeAt(coord: Coord): Ship? = board.shipAt(coord)?.also {
        board.remove(it)
        Timber.d("Removed %s", it)
    }

    fun randomize() {
        board = placer.createBoard(size, fleet)
        Timber.d("Fleet placed randomly")
    }

    fun clear() {
        board = Board(size)
    }
}
