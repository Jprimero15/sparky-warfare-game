package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Procedural cyber-neon tank renderer.
 *
 * Gameplay state stays in [Tank]; this class owns only visual geometry.
 * It uses ShapeRenderer so tank variants remain resolution-independent.
 */
class TankRenderer(private val shapeRenderer: ShapeRenderer) {
    private val points = FloatArray(16)
    private val glowColor = Color()
    private val hullColor = Color()
    private val accentColor = Color()

    fun drawTank(tank: Tank) {
        val radius = tank.radius
        val x = tank.position.x
        val y = tank.position.y
        val angle = tank.angle
        val tier = tank.enemyTier

        for (i in 4 downTo 1) {
            val halo = radius * (1.05f + i * 0.28f)
            shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.018f + i * 0.012f)
            shapeRenderer.circle(x, y, halo, 24)
        }

        val widthScale = when {
            tank.isPlayer -> 1f
            tier == EnemyTier.HEAVY -> 1.18f
            tier == EnemyTier.ELITE -> 1.12f
            tier == EnemyTier.RANGED -> 0.92f
            else -> 0.96f
        }
        val lengthScale = when {
            tank.isPlayer -> 1.18f
            tier == EnemyTier.RANGED -> 1.24f
            tier == EnemyTier.HEAVY -> 1.05f
            else -> 1.12f
        }
        val sides = if (tank.isPlayer || tier == EnemyTier.ELITE) 8 else 6
        val hullLength = radius * lengthScale
        val halfWidth = radius * widthScale * 0.72f

        setRotatedPolygon(x, y, angle, sides, hullLength, halfWidth)
        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.92f)
        fillPolygon(x, y, sides)

        setRotatedPolygon(x, y, angle, sides, hullLength * 0.72f, halfWidth * 0.76f)
        hullColor.set(tank.color.r * 0.14f, tank.color.g * 0.14f, tank.color.b * 0.14f, 0.9f)
        shapeRenderer.color = hullColor
        fillPolygon(x, y, sides)

        val rad = Math.toRadians(angle.toDouble())
        val forwardX = cos(rad).toFloat()
        val forwardY = sin(rad).toFloat()
        val sideX = -forwardY
        val sideY = forwardX
        val cannonStart = radius * 0.22f
        val cannonEnd = radius * if (tank.isPlayer) 1.82f else 1.68f
        val cannonWidth = radius * 0.22f

        val ax = x + forwardX * cannonStart
        val ay = y + forwardY * cannonStart
        val bx = x + forwardX * cannonEnd
        val by = y + forwardY * cannonEnd
        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.96f)
        drawThickLine(ax, ay, bx, by, cannonWidth * 2f)

        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(bx, by, radius * 0.14f, 10)

        val reactorRadius = radius * if (tank.isPlayer) 0.25f else 0.22f
        glowColor.set(tank.color.r, tank.color.g, tank.color.b, 1f)
        shapeRenderer.color = glowColor
        shapeRenderer.circle(x, y, reactorRadius * 1.7f, 16)
        shapeRenderer.color.set(0.8f, 0.98f, 1f, 0.95f)
        shapeRenderer.circle(x, y, reactorRadius * 0.72f, 12)

        val finLength = radius * if (tier == EnemyTier.HEAVY) 0.92f else 0.7f
        val finWidth = radius * 0.28f
        drawFin(x, y, forwardX, forwardY, sideX, sideY, radius * 0.3f, finLength, finWidth)
        drawFin(x, y, forwardX, forwardY, -sideX, -sideY, radius * 0.3f, finLength, finWidth)

        if (tier == EnemyTier.ELITE) {
            shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.72f)
            shapeRenderer.circle(x, y, radius * 0.82f, 8)
            shapeRenderer.color.set(1f, 1f, 1f, 0.48f)
            shapeRenderer.circle(x, y, radius * 0.9f, 8)
        }

        if (tank.isPlayer) {
            accentColor.set(1f, 1f, 1f, 0.62f)
            shapeRenderer.color = accentColor
            shapeRenderer.circle(
                x + forwardX * radius * 0.48f,
                y + forwardY * radius * 0.48f,
                radius * 0.09f,
                8
            )
        }

        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.9f)
        setRotatedPolygon(x, y, angle, sides, hullLength * 1.02f, halfWidth * 1.02f)
        strokePolygon(sides)
    }

    private fun drawFin(
        x: Float,
        y: Float,
        forwardX: Float,
        forwardY: Float,
        sideX: Float,
        sideY: Float,
        start: Float,
        length: Float,
        width: Float
    ) {
        val backX = x - forwardX * start
        val backY = y - forwardY * start
        val tipX = backX + forwardX * length
        val tipY = backY + forwardY * length
        shapeRenderer.color.set(0.9f, 0.98f, 1f, 0.3f)
        shapeRenderer.triangle(
            backX + sideX * width, backY + sideY * width,
            backX - sideX * width, backY - sideY * width,
            tipX, tipY
        )
    }

    private fun setRotatedPolygon(
        x: Float,
        y: Float,
        angle: Float,
        sides: Int,
        length: Float,
        halfWidth: Float
    ) {
        val rad = Math.toRadians(angle.toDouble())
        val fx = cos(rad).toFloat()
        val fy = sin(rad).toFloat()
        val sx = -fy
        val sy = fx
        for (i in 0 until sides) {
            val a = i.toFloat() / sides * 6.2831855f
            val localX = cos(a.toDouble()).toFloat() * length
            val localY = sin(a.toDouble()).toFloat() * halfWidth
            points[i * 2] = x + fx * localX + sx * localY
            points[i * 2 + 1] = y + fy * localX + sy * localY
        }
    }

    private fun fillPolygon(cx: Float, cy: Float, sides: Int) {
        for (i in 0 until sides) {
            val next = (i + 1) % sides
            shapeRenderer.triangle(cx, cy, points[i * 2], points[i * 2 + 1], points[next * 2], points[next * 2 + 1])
        }
    }

    private fun strokePolygon(sides: Int) {
        for (i in 0 until sides) {
            val next = (i + 1) % sides
            shapeRenderer.line(points[i * 2], points[i * 2 + 1], points[next * 2], points[next * 2 + 1])
        }
    }

    private fun drawThickLine(ax: Float, ay: Float, bx: Float, by: Float, thickness: Float) {
        val dx = bx - ax
        val dy = by - ay
        val length = sqrt(dx * dx + dy * dy)
        if (length < 0.0001f) return
        val nx = -dy / length * thickness / 2f
        val ny = dx / length * thickness / 2f
        shapeRenderer.triangle(ax + nx, ay + ny, ax - nx, ay - ny, bx - nx, by - ny)
        shapeRenderer.triangle(ax + nx, ay + ny, bx - nx, by - ny, bx + nx, by + ny)
    }
}
