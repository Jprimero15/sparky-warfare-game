package com.sparkywarfare.game

/**
 * Available simulation modes. Only SINGLE_PLAYER is active today.
 * Future multiplayer transports can select a mode without changing rendering code.
 */
enum class GameMode {
    SINGLE_PLAYER,
    LOCAL_MULTIPLAYER,
    LAN_MULTIPLAYER
}

/** Stable identity for a human-controlled participant. */
data class PlayerSlot(
    val id: Int,
    var tank: Tank? = null,
    val local: Boolean = false
)

/**
 * Lightweight session boundary between the game simulation and a future transport layer.
 * It deliberately contains no networking code.
 */
class GameSession {
    var mode: GameMode = GameMode.SINGLE_PLAYER
        private set

    var localPlayerId: Int = 0
        private set

    private val slots = mutableListOf<PlayerSlot>()

    val players: List<PlayerSlot>
        get() = slots

    val humanPlayers: List<PlayerSlot>
        get() = slots.filter { it.tank?.isPlayer == true }

    /** Enemy AI remains enabled only for the current single-player mode. */
    val aiEnabled: Boolean
        get() = mode == GameMode.SINGLE_PLAYER

    fun beginSinglePlayer(tank: Tank) {
        mode = GameMode.SINGLE_PLAYER
        localPlayerId = 0
        slots.clear()
        slots.add(PlayerSlot(id = localPlayerId, tank = tank, local = true))
    }

    /** Reserved for a future multiplayer session; intentionally unused for now. */
    fun clear() {
        slots.clear()
        localPlayerId = 0
        mode = GameMode.SINGLE_PLAYER
    }
}
