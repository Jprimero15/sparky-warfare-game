package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Procedural cyber-neon tank renderer.
 *
 * Hull geometry is tier-specific while the weapon turret can aim independently
 * from hull movement. No bitmap tank assets are required.
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
        val hullAngle = tank.angle
        val turretAngle = tank.turretAngle
        val tier = tank.enemyTier

        for (i in 4 downTo 1) {
            val halo = radius * (1.05f + i * 0.28f)
            shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.018f + i * 0.012f)
            shapeRenderer.circle(x, y, halo, 24)
        }

        val widthScale = when {
            tank.isPlayer -> 1f
            tier == EnemyTier.HEAVY -> 1.22f
            tier == EnemyTier.ELITE -> 1.14f
            tier == EnemyTier.RANGED -> 0.88f
            tier == EnemyTier.SCOUT -> 0.78f
            else -> 0.96f
        }
        val lengthScale = when {
            tank.isPlayer -> 1.18f
            tier == EnemyTier.RANGED -> 1.32f
            tier == EnemyTier.HEAVY -> 1.02f
            tier == EnemyTier.SCOUT -> 1.34f
            else -> 1.12f
        }

        val sides = when {
            tank.isPlayer -> 8
            tier == EnemyTier.SCOUT -> 5
            tier == EnemyTier.HEAVY -> 8
            tier == EnemyTier.RANGED -> 6
            tier == EnemyTier.ELITE -> 8
            else -> 6
        }
        val hullLength = radius * lengthScale
        val halfWidth = radius * widthScale * 0.72f

        setRotatedPolygon(x, y, hullAngle, sides, hullLength, halfWidth)
        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.92f)
        fillPolygon(x, y, sides)

        setRotatedPolygon(x, y, hullAngle, sides, hullLength * 0.72f, halfWidth * 0.76f)
        hullColor.set(tank.color.r * 0.14f, tank.color.g * 0.14f, tank.color.b * 0.14f, 0.9f)
        shapeRenderer.color = hullColor
        fillPolygon(x, y, sides)

        // Turret mount stays centered on the hull.
        val turretRad = Math.toRadians(turretAngle.toDouble())
        val forwardX = cos(turretRad).toFloat()
        val forwardY = sin(turretRad).toFloat()
        val sideX = -forwardY
        val sideY = forwardX

        val mountRadius = when (tier) {
            EnemyTier.HEAVY -> radius * 0.42f
            EnemyTier.RANGED -> radius * 0.34f
            EnemyTier.ELITE -> radius * 0.46f
            EnemyTier.SCOUT -> radius * 0.27f
            else -> radius * 0.34f
        }
        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.72f)
        shapeRenderer.circle(x, y, mountRadius, if (tier == EnemyTier.ELITE) 10 else 8)
        shapeRenderer.color.set(0.015f, 0.025f, 0.04f, 0.98f)
        shapeRenderer.circle(x, y, mountRadius * 0.68f, 8)

        val cannonStart = mountRadius * 0.35f
        val cannonEnd = radius * when {
            tank.isPlayer -> 1.82f
            tier == EnemyTier.RANGED -> 2.08f
            tier == EnemyTier.HEAVY -> 1.58f
            tier == EnemyTier.SCOUT -> 1.5f
            else -> 1.68f
        }
        val cannonWidth = radius * when {
            tier == EnemyTier.HEAVY -> 0.30f
            tier == EnemyTier.RANGED -> 0.18f
            tier == EnemyTier.SCOUT -> 0.15f
            tier == EnemyTier.ELITE -> 0.27f
            else -> 0.22f
        }

        val ax = x + forwardX * cannonStart
        val ay = y + forwardY * cannonStart
        val bx = x + forwardX * cannonEnd
        val by = y + forwardY * cannonEnd

        shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.96f)
        drawThickLine(ax, ay, bx, by, cannonWidth * 2f)

        // Tier-specific muzzle/weapon signatures.
        when (tier) {
            EnemyTier.RANGED -> {
                shapeRenderer.color.set(0.85f, 0.95f, 1f, 0.95f)
                shapeRenderer.circle(bx, by, radius * 0.17f, 10)
                shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.42f)
                shapeRenderer.circle(bx, by, radius * 0.29f, 16)
            }
            EnemyTier.HEAVY -> {
                shapeRenderer.color.set(1f, 0.72f, 0.25f, 0.95f)
                shapeRenderer.circle(bx, by, radius * 0.20f, 10)
            }
            EnemyTier.ELITE -> {
                shapeRenderer.color.set(1f, 0.9f, 1f, 0.98f)
                shapeRenderer.circle(bx, by, radius * 0.16f, 10)
                shapeRenderer.color.set(tank.color.r, tank.color.g, tank.color.b, 0.65f)
                shapeRenderer.circle(bx, by, radius * 0.34f, 12)
            }
            EnemyTier.SCOUT -> {
                shapeRenderer.color.set(0.75f, 1f, 1f, 0.95f)
                shapeRenderer.circle(bx, by, radius * 0.11f, 8)
            }
            else -> {
                shapeRenderer.color = Color.WHITE
                shapeRenderer.circle(bx, by, radius * 0.14f, 10)
            }
        }

        val reactorRadius = radius * if (tank.isPlayer) 0.25f else 0.22f
        glowColor.set(tank.color.r, tank.color.g, tank.color.b, 1f)
        shapeRenderer.color = glowColor
        shapeRenderer.circle(x, y, reactorRadius * 1.7f, 16)
        shapeRenderer.color.set(0.8f, 0.98f, 1f, 0.95f)
        shapeRenderer.circle(x, y, reactorRadius * 0.72f, 12)

        val finLength = radius * when (tier) {
            EnemyTier.HEAVY -> 1.05f
            EnemyTier.SCOUT -> 0.5f
            EnemyTier.RANGED -> 0.8f
            else -> 0.7f
        }
        val finWidth = radius * if (tier == EnemyTier.HEAVY) 0.34f else 0.28f
        drawFin(x, y, hullAngle, sideSign = 1f, start = radius * 0.3f, length = finLength, width = finWidth)
        drawFin(x, y, hullAngle, sideSign = -1f, start = radius * 0.3f, length = finLength, width = finWidth)

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
        setRotatedPolygon(x, y, hullAngle, sides, hullLength * 1.02f, halfWidth * 1.02f)
        strokePolygon(sides)
    }

    private fun drawFin(
        x: Float,
        y: Float,
        hullAngle: Float,
        sideSign: Float,
        start: Float,
        length: Float,
        width: Float
    ) {
        val rad = Math.toRadians(hullAngle.toDouble())
        val forwardX = cos(rad).toFloat()
        val forwardY = sin(rad).toFloat()
        val sideX = -forwardY * sideSign
        val sideY = forwardX * sideSign
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
            shapeRenderer.triangle(
                cx, cy,
                points[i * 2], points[i * 2 + 1],
                points[next * 2], points[next * 2 + 1]
            )
        }
    }

    private fun strokePolygon(sides: Int) {
        for (i in 0 until sides) {
            val next = (i + 1) % sides
            shapeRenderer.line(
                points[i * 2], points[i * 2 + 1],
                points[next * 2], points[next * 2 + 1]
            )
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
