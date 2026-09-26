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
        WallType.STEEL -> UiTheme.WALL_STEEL
        WallType.BRICK -> UiTheme.WALL_BLUE
        WallType.RED_BRICK -> UiTheme.WALL_VIOLET
        WallType.CONCRETE -> UiTheme.WALL_BLUE
        WallType.METAL -> UiTheme.WALL_CYAN
    }

    fun hit() {
        if (type == WallType.STEEL) return
        hitPoints -= 1
        if (hitPoints <= 0) alive = false
    }
}
