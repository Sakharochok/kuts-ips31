package com.kuts.battleship.game

/** Клітинка поля: рядок і стовпець, нумерація з нуля. */
data class Coord(val row: Int, val col: Int) {

    fun isInside(size: Int): Boolean = row in 0 until size && col in 0 until size

    /** Вісім сусідніх клітинок (включно з діагональними), без перевірки меж поля. */
    fun neighbors(): List<Coord> =
        (-1..1).flatMap { dr -> (-1..1).map { dc -> Coord(row + dr, col + dc) } }
            .filter { it != this }

    /** Чотири сусіди по горизонталі та вертикалі, без перевірки меж поля. */
    fun orthogonalNeighbors(): List<Coord> =
        listOf(Coord(row - 1, col), Coord(row + 1, col), Coord(row, col - 1), Coord(row, col + 1))
}
