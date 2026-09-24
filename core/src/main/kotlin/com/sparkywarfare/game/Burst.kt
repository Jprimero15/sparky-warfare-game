package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class Burst {
    val position = Vector2()
    val color = Color(1f, 1f, 1f, 1f)
    var t = 0f
    var active = false

    fun reset(position: Vector2, sourceColor: Color) {
        this.position.set(position)
        color.set(sourceColor)
        t = 0f
        active = true
    }

    fun reset() {
        active = false
        t = 0f
    }
}
