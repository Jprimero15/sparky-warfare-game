package com.sparkywarfare.game

import com.badlogic.gdx.math.Rectangle

/** Builds a small pool of authored arenas while preserving the vector/glow renderer. */
class ArenaBuilder {
    private lateinit var target: MutableList<Wall>

    fun build(walls: MutableList<Wall>, variant: Int) {
        target = walls
        walls.clear()
        val cols = (WORLD_WIDTH / TILE).toInt()
        val rows = (WORLD_HEIGHT / TILE).toInt()

        for (c in 0 until cols) {
            walls.add(wallAt(c, 0, WallType.STEEL))
            walls.add(wallAt(c, rows - 1, WallType.STEEL))
        }
        for (r in 0 until rows) {
            walls.add(wallAt(0, r, WallType.STEEL))
            walls.add(wallAt(cols - 1, r, WallType.STEEL))
        }

        when ((variant % 3 + 3) % 3) {
            0 -> classic(cols)
            1 -> offset(cols)
            else -> crossfire(cols)
        }

        val spawnLane = Rectangle(WORLD_WIDTH / 2f - 90f, 80f, 180f, 120f)
        walls.removeAll { it.bounds.overlaps(spawnLane) }
    }

    private fun classic(cols: Int) {
        add(5, 5, 4, 1, WallType.BRICK)
        add(5, 6, 1, 3, WallType.RED_BRICK)
        add(8, 8, 3, 1, WallType.CONCRETE)
        add(cols - 9, 5, 4, 1, WallType.RED_BRICK)
        add(cols - 6, 6, 1, 3, WallType.BRICK)
        add(cols - 11, 8, 3, 1, WallType.METAL)
        add(17, 10, 2, 3, WallType.CONCRETE)
        add(19, 12, 3, 1, WallType.BRICK)
        add(cols - 22, 10, 2, 3, WallType.CONCRETE)
        add(cols - 21, 12, 3, 1, WallType.RED_BRICK)
        add(28, 15, 4, 1, WallType.METAL)
        add(30, 16, 1, 2, WallType.BRICK)
        add(cols - 32, 15, 4, 1, WallType.METAL)
        add(cols - 31, 16, 1, 2, WallType.BRICK)
    }

    private fun offset(cols: Int) {
        add(7, 4, 5, 1, WallType.BRICK)
        add(8, 5, 1, 4, WallType.CONCRETE)
        add(12, 9, 3, 2, WallType.METAL)
        add(14, 12, 4, 1, WallType.RED_BRICK)
        add(cols - 13, 5, 4, 1, WallType.RED_BRICK)
        add(cols - 11, 6, 1, 4, WallType.CONCRETE)
        add(cols - 18, 10, 4, 2, WallType.BRICK)
        add(cols - 21, 13, 3, 1, WallType.METAL)
        add(23, 16, 3, 1, WallType.CONCRETE)
        add(26, 11, 2, 4, WallType.BRICK)
        add(cols - 28, 16, 3, 1, WallType.CONCRETE)
        add(cols - 28, 11, 2, 4, WallType.RED_BRICK)
    }

    private fun crossfire(cols: Int) {
        add(9, 5, 2, 4, WallType.CONCRETE)
        add(12, 7, 5, 1, WallType.BRICK)
        add(17, 5, 2, 2, WallType.RED_BRICK)
        add(cols - 11, 5, 2, 4, WallType.CONCRETE)
        add(cols - 17, 7, 5, 1, WallType.RED_BRICK)
        add(cols - 19, 5, 2, 2, WallType.BRICK)
        add(22, 11, 2, 5, WallType.METAL)
        add(cols - 24, 11, 2, 5, WallType.METAL)
        add(29, 14, 4, 1, WallType.BRICK)
        add(cols - 33, 14, 4, 1, WallType.RED_BRICK)
    }

    private fun add(col: Int, row: Int, width: Int, height: Int, type: WallType) {
        val cols = (WORLD_WIDTH / TILE).toInt()
        val rows = (WORLD_HEIGHT / TILE).toInt()
        for (x in col until col + width) {
            for (y in row until row + height) {
                if (x in 1 until cols - 1 && y in 1 until rows - 1) {
                    target.add(wallAt(x, y, type))
                }
            }
        }
    }

    private fun wallAt(col: Int, row: Int, type: WallType) =
        Wall(Rectangle(col * TILE, row * TILE, TILE, TILE), type)
}
