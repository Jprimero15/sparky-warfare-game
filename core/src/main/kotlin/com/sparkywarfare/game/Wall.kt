package com.sparkywarfare.game

import com.badlogic.gdx.math.Rectangle

enum class WallType { BRICK, STEEL }

/**
 * A destructible (BRICK) or indestructible (STEEL) wall tile.
 * Rendered with a faint energy-crack glow as it takes damage.
 */
class Wall(
    val bounds: Rectangle,
    val type: WallType,
    var hitPoints: Int = if (type == WallType.BRICK) 2 else Int.MAX_VALUE
) {
    var alive = true

    fun hit() {
        if (type == WallType.STEEL) return
        hitPoints -= 1
        if (hitPoints <= 0) alive = false
    }
}
