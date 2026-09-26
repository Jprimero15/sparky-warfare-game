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
        val minX = floor(min(a.x, b.x) / cellSize).toInt() - 1
        val maxX = floor(max(a.x, b.x) / cellSize).toInt() + 1
        val minY = floor(min(a.y, b.y) / cellSize).toInt() - 1
        val maxY = floor(max(a.y, b.y) / cellSize).toInt() + 1

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val list = cells[key(x, y)] ?: continue
                for (wall in list) {
                    if (wall.alive && CollisionSystem.segmentIntersectsRectangle(a, b, wall.bounds)) return true
                }
            }
        }
        return false
    }

    private fun key(x: Int, y: Int): Long =
        (x.toLong() shl 32) xor (y.toLong() and 0xffffffffL)
}
