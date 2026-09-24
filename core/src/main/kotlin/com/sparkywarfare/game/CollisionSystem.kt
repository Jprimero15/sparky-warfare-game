package com.sparkywarfare.game

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

object CollisionSystem {
    private val closestPoint = Vector2()
    private val moveX = Vector2()
    private val moveY = Vector2()

    fun circleIntersectsRectangle(center: Vector2, radius: Float, rect: Rectangle): Boolean {
        val closestX = MathUtils.clamp(center.x, rect.x, rect.x + rect.width)
        val closestY = MathUtils.clamp(center.y, rect.y, rect.y + rect.height)
        val dx = center.x - closestX
        val dy = center.y - closestY
        return dx * dx + dy * dy < radius * radius
    }

    fun segmentHitsCircle(start: Vector2, end: Vector2, center: Vector2, radius: Float): Boolean {
        Intersector.nearestSegmentPoint(start, end, center, closestPoint)
        return closestPoint.dst2(center) <= radius * radius
    }

    fun hasLineOfSight(start: Vector2, end: Vector2, walls: List<Wall>): Boolean =
        walls.none { it.alive && Intersector.intersectSegmentRectangle(start, end, it.bounds) }

    fun tryMoveTank(tank: Tank, direction: Vector2, distance: Float, player: Tank, enemies: List<Tank>, walls: List<Wall>) {
        if (distance <= 0f) return
        moveX.set(tank.position).add(direction.x * distance, 0f)
        if (canOccupy(tank, moveX, player, enemies, walls)) tank.position.set(moveX)
        moveY.set(tank.position).add(0f, direction.y * distance)
        if (canOccupy(tank, moveY, player, enemies, walls)) tank.position.set(moveY)
        tank.position.x = MathUtils.clamp(tank.position.x, GameConfig.TILE + 1f, GameConfig.WORLD_WIDTH - GameConfig.TILE - 1f)
        tank.position.y = MathUtils.clamp(tank.position.y, GameConfig.TILE + 1f, GameConfig.WORLD_HEIGHT - GameConfig.TILE - 1f)
    }

    private fun canOccupy(tank: Tank, position: Vector2, player: Tank, enemies: List<Tank>, walls: List<Wall>): Boolean {
        if (walls.any { it.alive && circleIntersectsRectangle(position, tank.radius, it.bounds) }) return false
        if (enemies.any { it !== tank && it.alive && position.dst2(it.position) < (it.radius + tank.radius) * (it.radius + tank.radius) }) return false
        return tank === player || !player.alive || player.position.dst2(position) >= (player.radius + tank.radius) * (player.radius + tank.radius)
    }
}
