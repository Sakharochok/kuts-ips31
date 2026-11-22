package com.kuts.battleship.game

import timber.log.Timber
import kotlin.random.Random

/**
 * Суперник-комп'ютер. Бачить лише те, що бачив би гравець: куди вже стріляли
 * та результати власних пострілів.
 *
 * - Пошук: випадкова необстріляна клітинка; поки на полі немає однопалубних,
 *   стріляє лише "в шаховому порядку", бо будь-який корабль займає обидва кольори.
 * - Добивання: після влучання стріляє поруч, а коли напрямок відомий — уздовж лінії.
 */
class ComputerPlayer(
    private val random: Random,
    fleet: List<Int> = Board.FLEET,
) {
    private val remainingFleet = fleet.toMutableList()
    private val woundedCells = mutableListOf<Coord>()

    fun chooseTarget(board: Board): Coord {
        val targets = finishingTargets(board)
        if (targets.isNotEmpty()) {
            return targets.random(random).also { Timber.d("AI finishing ship at %s", it) }
        }
        return huntTarget(board).also { Timber.d("AI hunting at %s", it) }
    }

    fun onShotResult(target: Coord, result: ShotResult) {
        when (result) {
            ShotResult.Hit -> woundedCells += target
            is ShotResult.Sunk -> {
                woundedCells.removeAll(result.ship.cells)
                remainingFleet.remove(result.ship.size)
            }
            ShotResult.Miss, ShotResult.AlreadyShot -> Unit
        }
    }

    private fun finishingTargets(board: Board): List<Coord> {
        if (woundedCells.isEmpty()) return emptyList()

        if (woundedCells.size >= 2) {
            val sameRow = woundedCells.all { it.row == woundedCells[0].row }
            val sameCol = woundedCells.all { it.col == woundedCells[0].col }
            val lineEnds = when {
                sameRow -> {
                    val row = woundedCells[0].row
                    listOf(Coord(row, woundedCells.minOf { it.col } - 1), Coord(row, woundedCells.maxOf { it.col } + 1))
                }
                sameCol -> {
                    val col = woundedCells[0].col
                    listOf(Coord(woundedCells.minOf { it.row } - 1, col), Coord(woundedCells.maxOf { it.row } + 1, col))
                }
                else -> emptyList()
            }.filter { isOpen(board, it) }
            if (lineEnds.isNotEmpty()) return lineEnds
        }

        return woundedCells.flatMap { it.orthogonalNeighbors() }.distinct().filter { isOpen(board, it) }
    }

    private fun huntTarget(board: Board): Coord {
        val open = (0 until board.size).flatMap { row ->
            (0 until board.size).map { col -> Coord(row, col) }
        }.filter { !board.isShot(it) }
        check(open.isNotEmpty()) { "No cells left to shoot" }

        val useParity = (remainingFleet.minOrNull() ?: 1) >= 2
        val parityCells = if (useParity) open.filter { (it.row + it.col) % 2 == 0 } else emptyList()
        return parityCells.ifEmpty { open }.random(random)
    }

    private fun isOpen(board: Board, coord: Coord) = coord.isInside(board.size) && !board.isShot(coord)
}
