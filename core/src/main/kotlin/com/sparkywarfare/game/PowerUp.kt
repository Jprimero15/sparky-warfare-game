package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

enum class PowerUpType { RAPID_FIRE, SCORE_ORB, SHIELD, SPREAD_SHOT, OVERDRIVE }

class PowerUp(
    val position: Vector2,
    val type: PowerUpType
) {
    var alive = true
    var pulse = 0f
    val color = when (type) {
        PowerUpType.RAPID_FIRE -> Color(1f, 0.85f, 0.2f, 1f)
        PowerUpType.SCORE_ORB -> Color(0.6f, 1f, 0.4f, 1f)
        PowerUpType.SHIELD -> Color(0.25f, 0.75f, 1f, 1f)
        PowerUpType.SPREAD_SHOT -> Color(1f, 0.3f, 0.85f, 1f)
        PowerUpType.OVERDRIVE -> Color(0.35f, 1f, 0.85f, 1f)
    }

    fun update(delta: Float) {
        pulse += delta * 3f
    }
}
