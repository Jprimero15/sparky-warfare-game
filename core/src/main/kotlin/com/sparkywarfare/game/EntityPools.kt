package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool

class EntityPools {
    private val laserPool = object : Pool<Laser>(64, 512) {
        override fun newObject(): Laser = Laser()
    }
    private val burstPool = object : Pool<Burst>(64, 512) {
        override fun newObject(): Burst = Burst()
    }

    fun obtainLaser(position: Vector2, direction: Vector2, color: Color, firedByPlayer: Boolean): Laser {
        val laser = laserPool.obtain()
        laser.reset(position, direction, color, firedByPlayer)
        return laser
    }

    fun freeLaser(laser: Laser) {
        laserPool.free(laser)
    }

    fun obtainBurst(position: Vector2, color: Color): Burst {
        val burst = burstPool.obtain()
        burst.reset(position, color)
        return burst
    }

    fun freeBurst(burst: Burst) {
        burstPool.free(burst)
    }

    fun clear() {
        laserPool.clear()
        burstPool.clear()
    }
}
