package com.kuts.battleship.game

import timber.log.Timber
import kotlin.random.Random

enum class Phase { PLAYER_TURN, COMPUTER_TURN, PLAYER_WON, COMPUTER_WON }

data class Move(val target: Coord, val result: ShotResult)

/**
 * Одна партія "людина проти комп'ютера".
 * Хто влучив — стріляє ще раз; хід переходить до суперника лише після промаху.
 */
class Game(
    random: Random = Random.Default,
    placer: FleetPlacer = FleetPlacer(random),
    private val computer: ComputerPlayer = ComputerPlayer(random),
    val playerBoard: Board = placer.createBoard(),
    val computerBoard: Board = placer.createBoard(),
) {
    var phase: Phase = Phase.PLAYER_TURN
        private set

    val isOver: Boolean get() = phase == Phase.PLAYER_WON || phase == Phase.COMPUTER_WON

    init {
        require(playerBoard.ships.isNotEmpty() && computerBoard.ships.isNotEmpty()) { "Both fleets must be placed" }
        Timber.i("New game started")
    }

    fun playerShoot(target: Coord): ShotResult {
        check(phase == Phase.PLAYER_TURN) { "Not player's turn: $phase" }
        val result = computerBoard.shoot(target)
        Timber.d("Player shoots %s -> %s", target, result)

        when {
            computerBoard.allSunk -> finish(Phase.PLAYER_WON)
            result == ShotResult.Miss -> phase = Phase.COMPUTER_TURN
        }
        return result
    }

    fun computerShoot(): Move {
        check(phase == Phase.COMPUTER_TURN) { "Not computer's turn: $phase" }
        val target = computer.chooseTarget(playerBoard)
        val result = playerBoard.shoot(target)
        computer.onShotResult(target, result)
        Timber.d("Computer shoots %s -> %s", target, result)

        when {
            playerBoard.allSunk -> finish(Phase.COMPUTER_WON)
            result == ShotResult.Miss -> phase = Phase.PLAYER_TURN
        }
        return Move(target, result)
    }

    private fun finish(result: Phase) {
        phase = result
        Timber.i("Game over: %s", result)
    }
}
