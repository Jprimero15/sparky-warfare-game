package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle

enum class WallType { STEEL, BRICK, RED_BRICK, CONCRETE, METAL }

class Wall(
    val bounds: Rectangle,
    val type: WallType,
    var hitPoints: Int = when (type) {
        WallType.STEEL -> Int.MAX_VALUE
        WallType.BRICK -> 2
        WallType.RED_BRICK -> 3
        WallType.CONCRETE -> 4
        WallType.METAL -> 2
    }
) {
    var alive = true
    val color = when (type) {
        WallType.STEEL -> Color(0.10f, 0.13f, 0.17f, 1f)
        WallType.BRICK -> Color(0.18f, 0.075f, 0.045f, 1f)
        WallType.RED_BRICK -> Color(0.30f, 0.09f, 0.055f, 1f)
        WallType.CONCRETE -> Color(0.23f, 0.25f, 0.27f, 1f)
        WallType.METAL -> Color(0.10f, 0.20f, 0.24f, 1f)
    }

    fun hit() {
        if (type == WallType.STEEL) return
        hitPoints -= 1
        if (hitPoints <= 0) alive = false
    }
}
