package com.kuts.battleship.game

/** Стан клітинки так, як його бачить гравець. */
enum class CellState { EMPTY, SHIP, MISS, HIT, SUNK }

sealed interface ShotResult {
    data object Miss : ShotResult
    data object Hit : ShotResult
    data class Sunk(val ship: Ship) : ShotResult
    data object AlreadyShot : ShotResult
}

/**
 * Ігрове поле одного гравця: кораблі та клітинки, по яких уже стріляли.
 * Кораблі не можуть торкатися один одного навіть кутами.
 */
class Board(val size: Int = SIZE) {

    private val _ships = mutableListOf<Ship>()
    val ships: List<Ship> get() = _ships

    private val shots = mutableSetOf<Coord>()

    val remainingShips: Int get() = _ships.count { !it.isSunk }

    val allSunk: Boolean get() = _ships.isNotEmpty() && _ships.all { it.isSunk }

    fun shipAt(coord: Coord): Ship? = _ships.firstOrNull { coord in it }

    fun isShot(coord: Coord): Boolean = coord in shots

    fun canPlace(ship: Ship): Boolean =
        ship.cells.all { it.isInside(size) } &&
            ship.cells.none { cell -> (cell.neighbors() + cell).any { shipAt(it) != null } }

    fun place(ship: Ship) {
        require(canPlace(ship)) { "Cannot place $ship" }
        _ships += ship
    }

    /** Прибирає корабель під час розстановки (до початку бою). */
    fun remove(ship: Ship) {
        check(shots.isEmpty()) { "Ships cannot be moved after the battle has started" }
        _ships.remove(ship)
    }

    /**
     * Постріл у клітинку. Після потоплення корабля всі клітинки навколо нього
     * автоматично вважаються обстріляними, бо там кораблів бути не може.
     */
    fun shoot(coord: Coord): ShotResult {
        require(coord.isInside(size)) { "Shot outside the board: $coord" }
        if (!shots.add(coord)) return ShotResult.AlreadyShot

        val ship = shipAt(coord) ?: return ShotResult.Miss
        ship.hit(coord)
        if (!ship.isSunk) return ShotResult.Hit

        ship.cells.flatMap { it.neighbors() }
            .filter { it.isInside(size) }
            .forEach { shots += it }
        return ShotResult.Sunk(ship)
    }

    /** @param revealShips показувати неуражені кораблі (власне поле гравця). */
    fun cellState(coord: Coord, revealShips: Boolean): CellState {
        val ship = shipAt(coord)
        return when {
            ship != null && ship.isSunk -> CellState.SUNK
            ship != null && ship.isHitAt(coord) -> CellState.HIT
            coord in shots -> CellState.MISS
            ship != null && revealShips -> CellState.SHIP
            else -> CellState.EMPTY
        }
    }

    companion object {
        const val SIZE = 10

        /** Класичний флот: 1 чотирипалубний, 2 трипалубні, 3 двопалубні, 4 однопалубні. */
        val FLEET: List<Int> = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
    }
}
