package com.kuts.battleship.game

/** Корабель — пряма неперервна лінія клітинок по горизонталі або вертикалі. */
class Ship(val cells: List<Coord>) {

    private val hits = mutableSetOf<Coord>()

    init {
        require(cells.isNotEmpty()) { "Ship must have at least one cell" }
        val horizontal = cells.all { it.row == cells[0].row } &&
            cells.map { it.col }.sorted().zipWithNext().all { (a, b) -> b == a + 1 }
        val vertical = cells.all { it.col == cells[0].col } &&
            cells.map { it.row }.sorted().zipWithNext().all { (a, b) -> b == a + 1 }
        require(horizontal || vertical) { "Ship cells must form a straight line: $cells" }
    }

    val size: Int get() = cells.size

    val isSunk: Boolean get() = hits.size == cells.size

    operator fun contains(coord: Coord): Boolean = coord in cells

    /** Позначає влучання; повертає false, якщо клітинка не належить кораблю. */
    fun hit(coord: Coord): Boolean {
        if (coord !in cells) return false
        hits += coord
        return true
    }

    fun isHitAt(coord: Coord): Boolean = coord in hits

    override fun toString(): String = "Ship(size=$size, cells=$cells)"

    companion object {
        fun of(start: Coord, size: Int, horizontal: Boolean): Ship = Ship(
            List(size) { i ->
                if (horizontal) Coord(start.row, start.col + i) else Coord(start.row + i, start.col)
            }
        )
    }
}
