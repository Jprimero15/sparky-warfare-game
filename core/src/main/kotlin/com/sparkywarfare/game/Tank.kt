package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * A glowing energy tank. Instead of a sprite, it's drawn as a soft
 * additive-blended core + rim glow (see GlowRenderer). Swap in a
 * texture later without changing this class's contract.
 */
class Tank(
    val position: Vector2,
    var angle: Float = 0f,          // facing direction, degrees
    val isPlayer: Boolean = false,
    var color: Color = Color(0.2f, 0.8f, 1f, 1f), // default: cyan energy
    var speed: Float = 90f,          // units per second
    var health: Int = 3,
    var fireCooldown: Float = 0f,
    var fireRate: Float = 0.35f      // seconds between shots
) {
    var alive: Boolean = true
    private var rapidFireTimer: Float = 0f
    private val baseFireRate = fireRate

    fun canFire(): Boolean = fireCooldown <= 0f

    fun grantRapidFire(duration: Float = 6f) {
        fireRate = baseFireRate * 0.35f
        rapidFireTimer = duration
    }

    fun update(delta: Float) {
        if (fireCooldown > 0f) fireCooldown -= delta
        if (rapidFireTimer > 0f) {
            rapidFireTimer -= delta
            if (rapidFireTimer <= 0f) fireRate = baseFireRate
        }
    }

    fun hit() {
        health -= 1
        if (health <= 0) alive = false
    }
}
