package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Laser(
    val origin: Vector2,
    val direction: Vector2,
    val color: Color,
    val speed: Float = 420f,
    val length: Float = 18f,
    var life: Float = 1.4f,
    val firedByPlayer: Boolean
) {
    val position = Vector2(origin)
    val previousPosition = Vector2(origin)
    var alive = true

    fun update(delta: Float) {
        if (!alive) return
        previousPosition.set(position)
        position.mulAdd(direction, speed * delta)
        life -= delta
        if (life <= 0f) alive = false
    }
}
