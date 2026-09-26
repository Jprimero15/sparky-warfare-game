package com.sparkywarfare.game

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.MathUtils

class PowerUpManager {
    fun chooseType(wave: Int): PowerUpType {
        return when (MathUtils.random(0, if (wave >= 8) 4 else 3)) {
            0 -> PowerUpType.RAPID_FIRE
            1 -> PowerUpType.SCORE_ORB
            2 -> PowerUpType.SHIELD
            3 -> PowerUpType.SPREAD_SHOT
            else -> PowerUpType.OVERDRIVE
        }
    }
    fun isSafe(position: Vector2, player: Tank, walls: List<Wall>, powerUps: List<PowerUp>): Boolean {
        if (position.dst2(player.position) < 110f * 110f) return false
        for (wall in walls) {
            if (wall.alive && CollisionSystem.circleIntersectsRectangle(position, 13f, wall.bounds)) return false
        }
        for (powerUp in powerUps) {
            if (powerUp.alive && position.dst2(powerUp.position) < 52f * 52f) return false
        }
        return true
    }
}
