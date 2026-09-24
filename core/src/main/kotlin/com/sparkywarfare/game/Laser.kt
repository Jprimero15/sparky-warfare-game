package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * A laser bolt fired from a tank. Rendered as a thin glowing beam with
 * a fading trail (see GlowRenderer.drawLaser).
 */
class Laser(
    val origin: Vector2,
    val direction: Vector2,   // normalized
    val color: Color,
    val speed: Float = 420f,
    val length: Float = 18f,
    var life: Float = 1.4f,
    val firedByPlayer: Boolean
) {
    val position = Vector2(origin)
    var alive = true

    fun update(delta: Float) {
        position.mulAdd(direction, speed * delta)
        life -= delta
        if (life <= 0f) alive = false
    }
}
