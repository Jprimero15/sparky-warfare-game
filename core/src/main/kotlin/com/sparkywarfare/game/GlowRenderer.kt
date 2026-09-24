package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sin

/**
 * Shared VFX renderer. Everything uses layered glow + additive blending
 * so the battlefield reads clearly on mobile without external assets.
 */
class GlowRenderer(private val shapeRenderer: ShapeRenderer) {
    fun drawTank(position: Vector2, angle: Float, color: Color, radius: Float = 14f) {
        beginAdditive()
        for (i in 5 downTo 1) {
            val r = radius * (1f + i * 0.24f)
            shapeRenderer.color = Color(color.r, color.g, color.b, 0.025f + i * 0.015f)
            shapeRenderer.circle(position.x, position.y, r, 32)
        }
        shapeRenderer.color = Color(0.01f, 0.035f, 0.06f, 0.95f)
        shapeRenderer.circle(position.x, position.y, radius * 0.98f, 28)
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.95f)
        shapeRenderer.circle(position.x, position.y, radius * 0.66f, 24)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.55f)
        shapeRenderer.circle(position.x - radius * 0.22f, position.y + radius * 0.2f, radius * 0.18f, 12)

        val rad = Math.toRadians(angle.toDouble())
        val bx = position.x + Math.cos(rad).toFloat() * radius * 1.7f
        val by = position.y + Math.sin(rad).toFloat() * radius * 1.7f
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.9f)
        drawThickLine(position, Vector2(bx, by), radius * 0.34f)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(bx, by, radius * 0.18f, 12)
        end()
    }

    fun drawLaser(laser: Laser) {
        beginAdditive()
        val tail = Vector2(laser.direction).scl(-laser.length * 1.6f).add(laser.position)
        shapeRenderer.color = Color(laser.color.r, laser.color.g, laser.color.b, 0.12f)
        drawThickLine(tail, laser.position, 14f)
        shapeRenderer.color = Color(laser.color.r, laser.color.g, laser.color.b, 0.35f)
        drawThickLine(tail, laser.position, 7f)
        shapeRenderer.color = Color.WHITE
        drawThickLine(tail, laser.position, 2f)
        end()
    }

    fun drawBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 48f) {
        beginAdditive()
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val radius = maxRadius * progress
        shapeRenderer.color = Color(color.r, color.g, color.b, alpha * 0.18f)
        shapeRenderer.circle(position.x, position.y, radius * 1.35f, 32)
        shapeRenderer.color = Color(color.r, color.g, color.b, alpha * 0.5f)
        shapeRenderer.circle(position.x, position.y, radius, 32)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(position.x, position.y, radius * 0.16f, 16)
        end()
    }

    fun drawPowerUp(position: Vector2, pulse: Float, color: Color) {
        beginAdditive()
        val r = 11f + sin(pulse.toDouble()).toFloat() * 3f
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.12f)
        shapeRenderer.circle(position.x, position.y, r * 2.8f, 32)
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.5f)
        shapeRenderer.circle(position.x, position.y, r * 1.5f, 24)
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.95f)
        shapeRenderer.circle(position.x, position.y, r, 20)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(position.x, position.y, r * 0.32f, 16)
        end()
    }

    fun drawDomainBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 220f) {
        beginAdditive()
        for (i in 0..3) {
            val p = (progress - i * 0.12f).coerceIn(0f, 1f)
            val radius = maxRadius * p
            shapeRenderer.color = Color(color.r, color.g, color.b, (1f - p) * 0.22f)
            shapeRenderer.circle(position.x, position.y, radius, 40)
        }
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(position.x, position.y, maxRadius * 0.1f * (1f - progress), 24)
        end()
    }

    private fun drawThickLine(a: Vector2, b: Vector2, thickness: Float) {
        val dir = Vector2(b).sub(a)
        if (dir.len2() < 0.0001f) return
        dir.nor()
        val normal = Vector2(-dir.y, dir.x).scl(thickness / 2f)
        val p1 = Vector2(a).add(normal)
        val p2 = Vector2(a).sub(normal)
        val p3 = Vector2(b).sub(normal)
        val p4 = Vector2(b).add(normal)
        shapeRenderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
        shapeRenderer.triangle(p1.x, p1.y, p3.x, p3.y, p4.x, p4.y)
    }

    private fun beginAdditive() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun end() = shapeRenderer.end()
}
