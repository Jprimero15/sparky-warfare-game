package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/** Reusable rounded primitives for the Soft Neon Arcade UI. */
object UiShapes {
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

    fun softButton(shape: ShapeRenderer, rect: com.badlogic.gdx.math.Rectangle, color: Color, radius: Float = 22f) {
        roundedRect(shape, rect.x, rect.y, rect.width, rect.height, radius, color)
        roundedRect(shape, rect.x + 4f, rect.y + rect.height - 6f, rect.width - 8f, 3f, 1.5f,
            Color(color.r, color.g, color.b, 0.20f))
    }
}