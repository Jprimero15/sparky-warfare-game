package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

/**
 * Single shared visual system for the whole "glowing energy" identity:
 * tank cores, laser beams, and explosion bursts all go through here.
 * This is the one piece every entity in the game reuses instead of
 * each having its own bespoke effect.
 *
 * Uses additive blending (GL_SRC_ALPHA, GL_ONE) so overlapping glows
 * brighten instead of muddying — the core trick behind this whole look.
 */
class GlowRenderer(private val shapeRenderer: ShapeRenderer) {

    /** Draw a tank as a glowing rim + bright core, facing `angle` degrees. */
    fun drawTank(position: Vector2, angle: Float, color: Color, radius: Float = 14f) {
        beginAdditive()

        // outer soft rim (3 falling-alpha rings = cheap glow falloff)
        for (i in 3 downTo 1) {
            val r = radius * (1f + i * 0.35f)
            val a = 0.12f / i
            shapeRenderer.color = Color(color.r, color.g, color.b, a)
            shapeRenderer.circle(position.x, position.y, r, 24)
        }

        // bright core
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.95f)
        shapeRenderer.circle(position.x, position.y, radius * 0.55f, 20)

        // facing indicator (barrel nub)
        val rad = Math.toRadians(angle.toDouble())
        val bx = position.x + Math.cos(rad).toFloat() * radius * 1.4f
        val by = position.y + Math.sin(rad).toFloat() * radius * 1.4f
        shapeRenderer.color = Color(1f, 1f, 1f, 0.85f)
        shapeRenderer.circle(bx, by, radius * 0.22f, 12)

        end()
    }

    /** Draw a laser bolt as a bright core line with a soft glow halo. */
    fun drawLaser(laser: Laser) {
        beginAdditive()
        val tail = Vector2(laser.direction).scl(-laser.length).add(laser.position)

        // glow halo (thicker, transparent)
        shapeRenderer.color = Color(laser.color.r, laser.color.g, laser.color.b, 0.25f)
        drawThickLine(tail, laser.position, 7f)

        // bright core beam
        shapeRenderer.color = Color(1f, 1f, 1f, 0.9f)
        drawThickLine(tail, laser.position, 2.2f)
        end()
    }

    /** Expanding shockwave ring + fading core burst for explosions / impacts. */
    fun drawBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 40f) {
        beginAdditive()
        val ringRadius = maxRadius * progress
        val alpha = (1f - progress).coerceIn(0f, 1f)

        shapeRenderer.color = Color(color.r, color.g, color.b, alpha * 0.6f)
        shapeRenderer.circle(position.x, position.y, ringRadius, 28)

        shapeRenderer.color = Color(1f, 1f, 1f, alpha * 0.8f)
        shapeRenderer.circle(position.x, position.y, ringRadius * 0.35f, 20)
        end()
    }

    /** Small pulsing glow orb for an on-field power-up pickup. */
    fun drawPowerUp(position: Vector2, pulse: Float, color: Color) {
        beginAdditive()
        val r = 10f + kotlin.math.sin(pulse.toDouble()).toFloat() * 3f
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.3f)
        shapeRenderer.circle(position.x, position.y, r * 2f, 24)
        shapeRenderer.color = Color(color.r, color.g, color.b, 0.9f)
        shapeRenderer.circle(position.x, position.y, r, 16)
        end()
    }

    /**
     * The hero-moment effect: a large multi-ring radial burst, the same
     * "domain expansion" look from the original reference image. Used
     * when a power-up is collected.
     */
    fun drawDomainBurst(position: Vector2, progress: Float, color: Color, maxRadius: Float = 220f) {
        beginAdditive()
        for (i in 0..2) {
            val ringProgress = (progress - i * 0.15f).coerceIn(0f, 1f)
            val radius = maxRadius * ringProgress
            val alpha = (1f - ringProgress) * 0.5f
            shapeRenderer.color = Color(color.r, color.g, color.b, alpha)
            shapeRenderer.circle(position.x, position.y, radius, 40)
        }
        shapeRenderer.color = Color(1f, 1f, 1f, (1f - progress) * 0.9f)
        shapeRenderer.circle(position.x, position.y, maxRadius * 0.12f * (1.2f - progress), 24)
        end()
    }

    private fun drawThickLine(a: Vector2, b: Vector2, thickness: Float) {
        val dir = Vector2(b).sub(a)
        val len = dir.len()
        if (len < 0.001f) return
        dir.nor()
        val normal = Vector2(-dir.y, dir.x).scl(thickness / 2f)

        // two triangles forming a quad along the line
        val p1 = Vector2(a).add(normal)
        val p2 = Vector2(a).sub(normal)
        val p3 = Vector2(b).sub(normal)
        val p4 = Vector2(b).add(normal)
        shapeRenderer.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
        shapeRenderer.triangle(p1.x, p1.y, p3.x, p3.y, p4.x, p4.y)
    }

    private fun beginAdditive() {
        com.badlogic.gdx.Gdx.gl.glEnable(GL20.GL_BLEND)
        com.badlogic.gdx.Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun end() {
        shapeRenderer.end()
    }
}
