package com.sparkywarfare.game

import com.badlogic.gdx.math.Vector2

/**
 * Drag-based virtual joystick. Works with raw screen coordinates
 * (Y-down, as LibGDX's touch callbacks provide) and exposes a
 * normalized, Y-up `direction` vector so GameScreen can feed it
 * straight into the same movement code the keyboard path uses.
 */
class VirtualJoystick(private val maxRadius: Float = 60f) {
    private var pointer = -1
    private val center = Vector2()
    private val current = Vector2()
    val direction = Vector2()
    var active = false
        private set

    fun tryActivate(screenX: Float, screenY: Float, pointerId: Int): Boolean {
        if (active) return false
        pointer = pointerId
        center.set(screenX, screenY)
        current.set(screenX, screenY)
        active = true
        return true
    }

    fun drag(screenX: Float, screenY: Float, pointerId: Int) {
        if (!active || pointerId != pointer) return
        current.set(screenX, screenY)
        val delta = Vector2(current).sub(center)
        val clampedLen = delta.len().coerceAtMost(maxRadius)
        if (delta.len() > 0.001f) delta.nor().scl(clampedLen)
        // screen Y grows downward; flip so "drag up" maps to positive world Y
        direction.set(delta.x, -delta.y).scl(1f / maxRadius)
    }

    fun release(pointerId: Int) {
        if (pointerId != pointer) return
        active = false
        pointer = -1
        direction.set(0f, 0f)
    }

    /** Base/knob positions converted to bottom-up render coordinates. */
    fun centerForRender(screenHeight: Float) = Vector2(center.x, screenHeight - center.y)
    fun knobForRender(screenHeight: Float) = Vector2(current.x, screenHeight - current.y)
}
