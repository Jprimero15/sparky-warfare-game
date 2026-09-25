package com.sparkywarfare.game

class CombatSystem {
    fun hasLineOfSight(from: Tank, to: Tank, walls: List<Wall>): Boolean =
        CollisionSystem.hasLineOfSight(from.position, to.position, walls)

    fun scoreForKill(tank: Tank, combo: Int, multiplier: Int): Int {
        val base = when {
            tank.radius >= 22f -> 500
            tank.radius >= 18f -> 250
            tank.radius <= 11f -> 125
            else -> 175
        }
        return base * (1 + (combo.coerceIn(1, GameConfig.Combat.MAX_COMBO) - 1) / 2) * multiplier.coerceAtLeast(1)
    }
}
