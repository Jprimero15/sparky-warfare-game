package com.sparkywarfare.game

import com.badlogic.gdx.math.Vector2

class CombatSystem {
    fun hasLineOfSight(from: Tank, to: Tank, walls: List<Wall>): Boolean {
        val a = from.position
        val b = to.position
        return walls.none { it.alive && CollisionSystem.segmentIntersectsRectangle(a, b, it.bounds) }
    }

    fun tryDamage(target: Tank): Boolean = target.hit()

    fun scoreForKill(tank: Tank, combo: Int, multiplier: Int): Int {
        val base = when {
            tank.radius >= 22f -> 500
            tank.radius >= 18f -> 300
            tank.radius >= 14f -> 200
            else -> 125
        }
        return base * combo.coerceIn(1, 8) * multiplier.coerceAtLeast(1)
    }
}
