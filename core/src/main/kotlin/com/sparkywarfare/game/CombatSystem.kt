package com.sparkywarfare.game

class CombatSystem {
    fun hasLineOfSight(from: Tank, to: Tank, walls: List<Wall>): Boolean =
        CollisionSystem.hasLineOfSight(from.position, to.position, walls)

    fun scoreForKill(tank: Tank, combo: Int, multiplier: Int): Int {
        val base = tank.enemyTier?.scoreValue ?: 175
        return base * (1 + (combo.coerceIn(1, GameConfig.Combat.MAX_COMBO) - 1) / 2) * multiplier.coerceAtLeast(1)
    }
}
