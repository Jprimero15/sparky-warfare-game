package com.sparkywarfare.game

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

object CollisionSystem {
    private val circleProxy = com.badlogic.gdx.math.Circle()

    fun circleIntersectsRectangle(center: Vector2, radius: Float, rect: Rectangle): Boolean {
        circleProxy.set(center, radius)
        return Intersector.overlaps(circleProxy, rect)
    }

    fun segmentIntersectsRectangle(a: Vector2, b: Vector2, rect: Rectangle): Boolean =
        Intersector.intersectSegmentRectangle(a, b, rect)

    fun segmentHitsCircle(a: Vector2, b: Vector2, center: Vector2, radius: Float): Boolean =
        Intersector.intersectSegmentCircle(a, b, center, radius * radius)

    fun tryMoveTank(
        tank: Tank,
        direction: Vector2,
        distance: Float,
        player: Tank,
        enemies: List<Tank>,
        walls: List<Wall>
    ) {
        if (!tank.alive || distance <= 0f) return

        val oldX = tank.position.x
        val oldY = tank.position.y

        // Tanks are solid circles. Resolve each axis independently so movement
        // cannot tunnel through another tank during fast or diagonal motion.
        tank.position.x += direction.x * distance
        if (hitsSolidTank(tank, player, enemies) ||
            walls.any { it.alive && circleIntersectsRectangle(tank.position, tank.radius, it.bounds) }
        ) {
            tank.position.x = oldX
        }

        tank.position.y += direction.y * distance
        if (hitsSolidTank(tank, player, enemies) ||
            walls.any { it.alive && circleIntersectsRectangle(tank.position, tank.radius, it.bounds) }
        ) {
            tank.position.y = oldY
        }

        tank.position.x = tank.position.x.coerceIn(
            tank.radius + TILE,
            WORLD_WIDTH - tank.radius - TILE
        )
        tank.position.y = tank.position.y.coerceIn(
            tank.radius + TILE,
            WORLD_HEIGHT - tank.radius - TILE
        )
    }

    private fun hitsSolidTank(tank: Tank, player: Tank, enemies: List<Tank>): Boolean {
        if (tank !== player && player.alive && circlesOverlap(tank, player)) return true
        return enemies.any { it !== tank && it.alive && circlesOverlap(tank, it) }
    }

    private fun circlesOverlap(a: Tank, b: Tank): Boolean {
        val dx = b.position.x - a.position.x
        val dy = b.position.y - a.position.y
        val minDistance = a.radius + b.radius
        return dx * dx + dy * dy < minDistance * minDistance
    }

    fun hasLineOfSight(from: Vector2, to: Vector2, walls: List<Wall>): Boolean =
        walls.none { it.alive && segmentIntersectsRectangle(from, to, it.bounds) }

    fun separateCircles(a: Tank, b: Tank) {
        val dx = b.position.x - a.position.x
        val dy = b.position.y - a.position.y
        val minDistance = a.radius + b.radius
        val dist2 = dx * dx + dy * dy
        if (dist2 >= minDistance * minDistance || dist2 <= 0.0001f) return

        val dist = kotlin.math.sqrt(dist2)
        val push = (minDistance - dist) * 0.5f
        val nx = dx / dist
        val ny = dy / dist
        a.position.x -= nx * push
        a.position.y -= ny * push
        b.position.x += nx * push
        b.position.y += ny * push
    }
}
