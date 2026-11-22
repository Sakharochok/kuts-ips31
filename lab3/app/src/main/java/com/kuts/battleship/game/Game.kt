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
    private val placer: FleetPlacer = FleetPlacer(random),
    private val computer: ComputerPlayer = ComputerPlayer(random),
    playerBoard: Board = placer.createBoard(),
    val computerBoard: Board = placer.createBoard(),
) {
    var playerBoard: Board = playerBoard
        private set

    var phase: Phase = Phase.PLAYER_TURN
        private set

    /** Чи був уже хоч один постріл (після цього флот гравця переставляти не можна). */
    var isStarted: Boolean = false
        private set

    val isOver: Boolean get() = phase == Phase.PLAYER_WON || phase == Phase.COMPUTER_WON

    init {
        Timber.i("New game started")
    }

    fun shufflePlayerFleet() {
        check(!isStarted) { "Fleet can only be shuffled before the first shot" }
        playerBoard = placer.createBoard(playerBoard.size)
        Timber.d("Player fleet shuffled")
    }

    fun playerShoot(target: Coord): ShotResult {
        check(phase == Phase.PLAYER_TURN) { "Not player's turn: $phase" }
        val result = computerBoard.shoot(target)
        if (result != ShotResult.AlreadyShot) isStarted = true
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
