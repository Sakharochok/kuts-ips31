package com.kuts.battleship.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameTest {

    /** Гра з відомими полями: у кожного по одному однопалубному кораблю. */
    private fun smallGame(): Game {
        val random = Random(42)
        return Game(
            random = random,
            computer = ComputerPlayer(random, fleet = listOf(1)),
            playerBoard = Board().apply { place(Ship.of(Coord(0, 0), 1, true)) },
            computerBoard = Board().apply { place(Ship.of(Coord(9, 9), 1, true)) },
        )
    }

    @Test
    fun `player starts`() {
        val game = Game(Random(1))
        assertEquals(Phase.PLAYER_TURN, game.phase)
        assertFalse(game.isOver)
        assertEquals(Board.FLEET.size, game.playerBoard.ships.size)
        assertEquals(Board.FLEET.size, game.computerBoard.ships.size)
    }

    @Test
    fun `miss passes the turn to computer`() {
        val game = smallGame()

        assertEquals(ShotResult.Miss, game.playerShoot(Coord(5, 5)))
        assertEquals(Phase.COMPUTER_TURN, game.phase)
    }

    @Test
    fun `hit keeps the turn`() {
        val game = Game(
            playerBoard = Board().apply { place(Ship.of(Coord(0, 0), 1, true)) },
            computerBoard = Board().apply { place(Ship.of(Coord(9, 7), 3, true)) },
        )

        assertEquals(ShotResult.Hit, game.playerShoot(Coord(9, 8)))
        assertEquals(Phase.PLAYER_TURN, game.phase)
    }

    @Test
    fun `repeated shot keeps the turn`() {
        val game = smallGame()
        game.playerShoot(Coord(5, 5))
        while (game.phase == Phase.COMPUTER_TURN) game.computerShoot()

        assertEquals(ShotResult.AlreadyShot, game.playerShoot(Coord(5, 5)))
        assertEquals(Phase.PLAYER_TURN, game.phase)
    }

    @Test
    fun `sinking the last ship wins`() {
        val game = smallGame()

        assertTrue(game.playerShoot(Coord(9, 9)) is ShotResult.Sunk)
        assertEquals(Phase.PLAYER_WON, game.phase)
        assertTrue(game.isOver)
    }

    @Test
    fun `computer plays until it misses and can win`() {
        val game = smallGame()
        game.playerShoot(Coord(5, 5))

        var guard = 0
        while (!game.isOver) {
            if (game.phase == Phase.COMPUTER_TURN) {
                val move = game.computerShoot()
                if (move.result == ShotResult.Miss) assertEquals(Phase.PLAYER_TURN, game.phase)
            } else {
                // Гравець навмисно мимо: будь-яка необстріляна клітинка, крім корабля в (9, 9).
                val target = (0 until 100).map { Coord(it / 10, it % 10) }
                    .first { it != Coord(9, 9) && !game.computerBoard.isShot(it) }
                game.playerShoot(target)
            }
            check(++guard < 200)
        }
        assertEquals(Phase.COMPUTER_WON, game.phase)
        assertTrue(game.playerBoard.allSunk)
    }

    @Test
    fun `turns are enforced`() {
        val game = smallGame()
        assertThrows { game.computerShoot() }

        game.playerShoot(Coord(5, 5))
        assertThrows { game.playerShoot(Coord(6, 6)) }
    }

    @Test
    fun `game requires placed fleets`() {
        assertThrows { Game(playerBoard = Board()) }
    }
}
