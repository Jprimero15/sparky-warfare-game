package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sin

class GlowRenderer(private val shapeRenderer: ShapeRenderer) {
    private val dir = Vector2()
    private val normal = Vector2()

    fun beginAdditive() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    fun end() {
        shapeRenderer.end()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun drawLaser(laser: Laser) {
        val tailX = laser.position.x - laser.direction.x * laser.length * 1.6f
        val tailY = laser.position.y - laser.direction.y * laser.length * 1.6f
        shapeRenderer.color.set(laser.color.r, laser.color.g, laser.color.b, 0.12f)
        drawThickLine(tailX, tailY, laser.position.x, laser.position.y, 14f)
        shapeRenderer.color.set(laser.color.r, laser.color.g, laser.color.b, 0.35f)
        drawThickLine(tailX, tailY, laser.position.x, laser.position.y, 7f)
        shapeRenderer.color.set(Color.WHITE)
        drawThickLine(tailX, tailY, laser.position.x, laser.position.y, 2f)
    }

    fun drawBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 48f) {
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val radius = maxRadius * progress
        shapeRenderer.color.set(color.r, color.g, color.b, alpha * 0.18f)
        shapeRenderer.circle(position.x, position.y, radius * 1.35f, 24)
        shapeRenderer.color.set(color.r, color.g, color.b, alpha * 0.5f)
        shapeRenderer.circle(position.x, position.y, radius, 24)
        shapeRenderer.color.set(Color.WHITE)
        shapeRenderer.circle(position.x, position.y, radius * 0.16f, 12)
    }

    fun drawPowerUp(position: Vector2, pulse: Float, color: Color) {
        val r = 11f + sin(pulse.toDouble()).toFloat() * 3f
        shapeRenderer.color.set(color.r, color.g, color.b, 0.12f)
        shapeRenderer.circle(position.x, position.y, r * 2.8f, 24)
        shapeRenderer.color.set(color.r, color.g, color.b, 0.5f)
        shapeRenderer.circle(position.x, position.y, r * 1.5f, 20)
        shapeRenderer.color.set(color.r, color.g, color.b, 0.95f)
        shapeRenderer.circle(position.x, position.y, r, 16)
        shapeRenderer.color.set(Color.WHITE)
        shapeRenderer.circle(position.x, position.y, r * 0.32f, 12)
    }

    fun drawDomainBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 220f) {
        for (i in 0..3) {
            val p = (progress - i * 0.12f).coerceIn(0f, 1f)
            val radius = maxRadius * p
            shapeRenderer.color.set(color.r, color.g, color.b, (1f - p) * 0.22f)
            shapeRenderer.circle(position.x, position.y, radius, 28)
        }
        shapeRenderer.color.set(Color.WHITE)
        shapeRenderer.circle(position.x, position.y, maxRadius * 0.1f * (1f - progress), 16)
    }

    private fun drawThickLine(ax: Float, ay: Float, bx: Float, by: Float, thickness: Float) {
        dir.set(bx - ax, by - ay)
        if (dir.len2() < 0.0001f) return
        dir.nor()
        normal.set(-dir.y, dir.x).scl(thickness / 2f)
        val p1x = ax + normal.x
        val p1y = ay + normal.y
        val p2x = ax - normal.x
        val p2y = ay - normal.y
        val p3x = bx - normal.x
        val p3y = by - normal.y
        val p4x = bx + normal.x
        val p4y = by + normal.y
        shapeRenderer.triangle(p1x, p1y, p2x, p2y, p3x, p3y)
        shapeRenderer.triangle(p1x, p1y, p3x, p3y, p4x, p4y)
    }
}
