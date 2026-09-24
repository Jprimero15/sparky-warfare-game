package com.sparkywarfare.game

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

object CollisionSystem {
    fun circleIntersectsRectangle(center: Vector2, radius: Float, rect: Rectangle): Boolean =
        Intersector.overlaps(CircleProxy(center, radius), rect)

    fun segmentIntersectsRectangle(a: Vector2, b: Vector2, rect: Rectangle): Boolean =
        Intersector.intersectSegmentRectangle(a, b, rect)

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
