package com.sparkywarfare.game

import com.badlogic.gdx.math.Vector2

class VirtualJoystick(private val maxRadius: Float = 60f) {
    private var pointer = -1
    private val center = Vector2()
    private val current = Vector2()
    private val delta = Vector2()
    val direction = Vector2()
    var active = false
        private set

    fun tryActivate(screenX: Float, screenY: Float, pointerId: Int): Boolean {
        if (active) return false
        pointer = pointerId
        center.set(screenX, screenY)
        current.set(screenX, screenY)
        direction.setZero()
        active = true
        return true
    }

    fun drag(screenX: Float, screenY: Float, pointerId: Int) {
        if (!active || pointerId != pointer) return
        current.set(screenX, screenY)
        delta.set(current).sub(center)
        val length = delta.len()
        if (length > maxRadius) delta.scl(maxRadius / length)
        direction.set(delta.x, -delta.y).scl(1f / maxRadius)
    }

    fun release(pointerId: Int) {
        if (pointerId == pointer) reset()
    }

    fun reset() {
        active = false
        pointer = -1
        center.setZero()
        current.setZero()
        delta.setZero()
        direction.setZero()
    }

    fun centerForRender(screenHeight: Float, out: Vector2): Vector2 =
        out.set(center.x, screenHeight - center.y)

    fun knobForRender(screenHeight: Float, out: Vector2): Vector2 =
        out.set(current.x, screenHeight - current.y)
}
