package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

enum class PowerUpType { RAPID_FIRE, SCORE_ORB }

/**
 * A pickup dropped after clearing a wave. Collecting one triggers the
 * big "domain expansion" radial burst (GlowRenderer.drawDomainBurst) —
 * the visual hero-moment from the original reference image.
 */
class PowerUp(
    val position: Vector2,
    val type: PowerUpType
) {
    var alive = true
    var pulse = 0f

    val color: Color
        get() = when (type) {
            PowerUpType.RAPID_FIRE -> Color(1f, 0.85f, 0.2f, 1f)
            PowerUpType.SCORE_ORB -> Color(0.6f, 1f, 0.4f, 1f)
        }

    fun update(delta: Float) {
        pulse += delta * 3f
    }
}
