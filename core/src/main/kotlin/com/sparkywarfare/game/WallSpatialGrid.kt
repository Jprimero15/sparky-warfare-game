package com.sparkywarfare.game

import com.badlogic.gdx.math.Vector2
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Tile-aligned spatial index for wall queries.
 * Rebuilt only when the wall set changes.
 */
class WallSpatialGrid(
    private val cellSize: Float = TILE
) {
    private val cells = HashMap<Long, MutableList<Wall>>(256)

    fun rebuild(walls: List<Wall>) {
        cells.clear()
        for (wall in walls) {
            if (!wall.alive) continue
            val cellX = floor(wall.bounds.x / cellSize).toInt()
            val cellY = floor(wall.bounds.y / cellSize).toInt()
            cells.getOrPut(key(cellX, cellY)) { ArrayList(1) }.add(wall)
        }
    }

    fun intersectsCircle(center: Vector2, radius: Float): Boolean {
        val minX = floor((center.x - radius) / cellSize).toInt()
        val maxX = floor((center.x + radius) / cellSize).toInt()
        val minY = floor((center.y - radius) / cellSize).toInt()
        val maxY = floor((center.y + radius) / cellSize).toInt()

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val list = cells[key(x, y)] ?: continue
                for (wall in list) {
                    if (wall.alive && CollisionSystem.circleIntersectsRectangle(center, radius, wall.bounds)) return true
                }
            }
        }
        return false
    }

    fun intersectsSegment(a: Vector2, b: Vector2): Boolean {
        var cellX = floor(a.x / cellSize).toInt()
        var cellY = floor(a.y / cellSize).toInt()
        val targetX = floor(b.x / cellSize).toInt()
        val targetY = floor(b.y / cellSize).toInt()

        val dx = b.x - a.x
        val dy = b.y - a.y
        val stepX = when {
            dx > 0f -> 1
            dx < 0f -> -1
            else -> 0
        }
        val stepY = when {
            dy > 0f -> 1
            dy < 0f -> -1
            else -> 0
        }

        val tDeltaX = if (stepX == 0) Float.POSITIVE_INFINITY else cellSize / kotlin.math.abs(dx)
        val tDeltaY = if (stepY == 0) Float.POSITIVE_INFINITY else cellSize / kotlin.math.abs(dy)
        var tMaxX = if (stepX == 0) {
            Float.POSITIVE_INFINITY
        } else {
            val boundary = if (stepX > 0) (cellX + 1) * cellSize else cellX * cellSize
            (boundary - a.x) / dx
        }
        var tMaxY = if (stepY == 0) {
            Float.POSITIVE_INFINITY
        } else {
            val boundary = if (stepY > 0) (cellY + 1) * cellSize else cellY * cellSize
            (boundary - a.y) / dy
        }

        while (true) {
            val list = cells[key(cellX, cellY)]
            if (list != null) {
                for (wall in list) {
                    if (wall.alive && CollisionSystem.segmentIntersectsRectangle(a, b, wall.bounds)) return true
                }
            }
            if (cellX == targetX && cellY == targetY) return false

            if (tMaxX < tMaxY) {
                cellX += stepX
                tMaxX += tDeltaX
            } else {
                cellY += stepY
                tMaxY += tDeltaY
            }
        }
    }

    private fun key(x: Int, y: Int): Long =
        (x.toLong() shl 32) xor (y.toLong() and 0xffffffffL)
}
