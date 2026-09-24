package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * A glowing energy tank. Rendering is handled by GlowRenderer.
 */
class Tank(
    val position: Vector2,
    var angle: Float = 0f,
    val isPlayer: Boolean = false,
    var color: Color = Color(0.2f, 0.8f, 1f, 1f),
    var speed: Float = 90f,
    var health: Int = 3,
    var fireCooldown: Float = 0f,
    var fireRate: Float = 0.35f,
    var radius: Float = 14f
) {
    var alive: Boolean = true
    var aiFireTimer: Float = 0f
    private var rapidFireTimer: Float = 0f
    private val baseFireRate = fireRate

    fun canFire(): Boolean = alive && fireCooldown <= 0f

    fun grantRapidFire(duration: Float = 6f) {
        fireRate = baseFireRate * 0.35f
        rapidFireTimer = duration
    }

    fun update(delta: Float) {
        fireCooldown = (fireCooldown - delta).coerceAtLeast(0f)
        if (rapidFireTimer > 0f) {
            rapidFireTimer -= delta
            if (rapidFireTimer <= 0f) {
                rapidFireTimer = 0f
                fireRate = baseFireRate
            }
        }
    }

    fun hit() {
        if (!alive) return
        health = (health - 1).coerceAtLeast(0)
        if (health == 0) alive = false
    }
}
