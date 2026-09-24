package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Laser {
    val origin = Vector2()
    val direction = Vector2()
    val color = Color(1f, 1f, 1f, 1f)
    val position = Vector2()
    val previousPosition = Vector2()
    var speed = GameConfig.Combat.LASER_SPEED
    var length = GameConfig.Combat.LASER_LENGTH
    var life = GameConfig.Combat.LASER_LIFE
    var firedByPlayer = false
    var alive = false

    fun reset(origin: Vector2, direction: Vector2, color: Color, firedByPlayer: Boolean) {
        this.origin.set(origin)
        this.direction.set(direction).nor()
        this.color.set(color)
        this.position.set(origin)
        this.previousPosition.set(origin)
        this.speed = GameConfig.Combat.LASER_SPEED
        this.length = GameConfig.Combat.LASER_LENGTH
        this.life = GameConfig.Combat.LASER_LIFE
        this.firedByPlayer = firedByPlayer
        this.alive = true
    }

    fun update(delta: Float) {
        if (!alive) return
        previousPosition.set(position)
        position.mulAdd(direction, speed * delta)
        life -= delta
        if (life <= 0f) alive = false
    }

    fun clear() {
        alive = false
    }
}
