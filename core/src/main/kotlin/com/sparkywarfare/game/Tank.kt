package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Tank(
    val position: Vector2,
    var angle: Float = 0f,
    val isPlayer: Boolean = false,
    var color: Color = Color(0.2f, 0.8f, 1f, 1f),
    speed: Float = 90f,
    var health: Int = 3,
    var fireCooldown: Float = 0f,
    fireRate: Float = 0.35f,
    var radius: Float = 14f,
    val enemyTier: EnemyTier? = null
) {
    var maxHealth = health
    var alive = true
    var aiFireTimer = 0f
    var aiAimAngle = angle
    var aiReactionTimer = 0f
    /** Direction the weapon/turret is visually and physically aimed. */
    var turretAngle = angle

    private var baseSpeed = speed
    var speed = speed
        set(value) {
            field = value
            if (overdriveTimer <= 0f) baseSpeed = value
        }
    private var baseFireRate = fireRate
    var fireRate = fireRate
        set(value) {
            field = value
            if (rapidFireTimer <= 0f) baseFireRate = value
        }
    private var rapidFireTimer = 0f
    private var spreadTimer = 0f
    private var overdriveTimer = 0f
    var invulnerabilityTimer = 0f
        private set
    var shieldTimer = 0f
        private set

    fun canFire(): Boolean = alive && fireCooldown <= 0f

    fun grantRapidFire(duration: Float = GameConfig.PowerUps.RAPID_FIRE_DURATION) {
        fireRate = baseFireRate * 0.35f
        rapidFireTimer = maxOf(rapidFireTimer, duration)
    }

    fun grantShield(duration: Float = GameConfig.PowerUps.SHIELD_DURATION) {
        shieldTimer = maxOf(shieldTimer, duration)
    }

    fun grantSpreadShot(duration: Float = GameConfig.PowerUps.SPREAD_DURATION) {
        spreadTimer = maxOf(spreadTimer, duration)
    }

    fun grantOverdrive(duration: Float = GameConfig.PowerUps.OVERDRIVE_DURATION) {
        if (overdriveTimer <= 0f) baseSpeed = speed
        speed = baseSpeed * 1.35f
        overdriveTimer = maxOf(overdriveTimer, duration)
    }

    fun hasSpreadShot(): Boolean = spreadTimer > 0f
    fun isShielded(): Boolean = shieldTimer > 0f

    fun update(delta: Float) {
        fireCooldown = (fireCooldown - delta).coerceAtLeast(0f)
        invulnerabilityTimer = (invulnerabilityTimer - delta).coerceAtLeast(0f)
        shieldTimer = (shieldTimer - delta).coerceAtLeast(0f)

        if (rapidFireTimer > 0f) {
            rapidFireTimer -= delta
            if (rapidFireTimer <= 0f) fireRate = baseFireRate
        }
        if (spreadTimer > 0f) spreadTimer -= delta
        if (overdriveTimer > 0f) {
            overdriveTimer -= delta
            if (overdriveTimer <= 0f) speed = baseSpeed
        }
    }

    fun hit(): Boolean {
        if (!alive || invulnerabilityTimer > 0f || shieldTimer > 0f) return false
        health = (health - 1).coerceAtLeast(0)
        invulnerabilityTimer = GameConfig.Player.HIT_IFRAMES
        if (health == 0) alive = false
        return true
    }

    fun resetForPlayer() {
        alive = true
        maxHealth = GameConfig.Player.MAX_HP
        health = GameConfig.Player.START_HP
        speed = GameConfig.Player.SPEED
        fireRate = GameConfig.Player.FIRE_RATE
        fireCooldown = 0f
        invulnerabilityTimer = 0f
        shieldTimer = 0f
        rapidFireTimer = 0f
        spreadTimer = 0f
        overdriveTimer = 0f
        turretAngle = angle
    }
}
