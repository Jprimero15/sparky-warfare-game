package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

/** Reusable rounded primitives for the Soft Neon Arcade UI. */
object UiShapes {
    private val gradientColor = Color()
    fun roundedRect(shape: ShapeRenderer, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        val r = radius.coerceAtMost(minOf(width, height) * 0.5f).coerceAtLeast(0f)
        shape.color = color
        if (r <= 0.5f) {
            shape.rect(x, y, width, height)
            return
        }
        shape.rect(x + r, y, width - 2f * r, height)
        shape.rect(x, y + r, r, height - 2f * r)
        shape.rect(x + width - r, y + r, r, height - 2f * r)
        shape.circle(x + r, y + r, r, 20)
        shape.circle(x + width - r, y + r, r, 20)
        shape.circle(x + r, y + height - r, r, 20)
        shape.circle(x + width - r, y + height - r, r, 20)
    }

    /** Draws a smooth vertical gradient while preserving the rounded silhouette. */
    fun gradientRoundedRect(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        topColor: Color,
        bottomColor: Color,
        steps: Int = 10
    ) {
        val count = steps.coerceIn(4, 24)
        roundedRect(shape, x, y, width, height, radius, bottomColor)
        val bandHeight = height / count
        for (i in 0 until count) {
            val t = (i + 0.5f) / count
            gradientColor.set(
                topColor.r + (bottomColor.r - topColor.r) * t,
                topColor.g + (bottomColor.g - topColor.g) * t,
                topColor.b + (bottomColor.b - topColor.b) * t,
                topColor.a + (bottomColor.a - topColor.a) * t
            )
            val bandY = y + height - (i + 1) * bandHeight
            roundedRect(shape, x, bandY, width, bandHeight + 1.5f, radius, gradientColor)
        }
    }

    /** Shared full-screen dim layer used by modal screens. */
    fun overlay(shape: ShapeRenderer, width: Float, height: Float) {
        shape.color = UiTheme.OVERLAY
        shape.rect(0f, 0f, width, height)
    }

    /** Shared rounded modal surface with a subtle neon edge and inset surface. */
    fun panel(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.PANEL_RADIUS
    ) {
        roundedRect(shape, x, y, width, height, radius, UiTheme.PANEL_EDGE)
        val inset = UiTheme.Metrics.PANEL_INSET
        roundedRect(
            shape,
            x + inset,
            y + inset,
            (width - inset * 2f).coerceAtLeast(0f),
            (height - inset * 2f).coerceAtLeast(0f),
            (radius - inset).coerceAtLeast(0f),
            UiTheme.PANEL
        )
    }
    fun softButton(shape: ShapeRenderer, rect: Rectangle, color: Color, radius: Float = UiTheme.Metrics.BUTTON_RADIUS) {
        // Blend the requested semantic color with the shared cyan/violet palette so
        // every touch target has the soft two-tone arcade treatment.
        val top = Color(
            (color.r * 0.58f + 0.18f * 0.42f).coerceIn(0f, 1f),
            (color.g * 0.58f + 0.9f * 0.42f).coerceIn(0f, 1f),
            (color.b * 0.58f + 1f * 0.42f).coerceIn(0f, 1f),
            color.a
        )
        val bottom = Color(
            (color.r * 0.52f + 0.78f * 0.48f).coerceIn(0f, 1f),
            (color.g * 0.52f + 0.24f * 0.48f).coerceIn(0f, 1f),
            (color.b * 0.52f + 1f * 0.48f).coerceIn(0f, 1f),
            color.a
        )
        gradientRoundedRect(shape, rect.x, rect.y, rect.width, rect.height, radius, top, bottom, 10)
        roundedRect(shape, rect.x + 5f, rect.y + rect.height - 7f, rect.width - 10f, 3f, 1.5f,
            Color(1f, 1f, 1f, 0.12f))
    }
}