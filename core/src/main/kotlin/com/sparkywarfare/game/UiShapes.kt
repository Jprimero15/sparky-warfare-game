package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

/** Reusable glassmorphic primitives for the Soft Neon Arcade UI. */
object UiShapes {
    /** Shared UI pass state so translucent glass blends over the game world. */
    fun begin(shape: ShapeRenderer, type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Filled) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shape.begin(type)
    }

    fun end(shape: ShapeRenderer) {
        shape.end()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private val gradientColor = Color()
    private val topColor = Color()
    private val glowColor = Color()
    private val accentColor = Color()

    /** A single continuous rounded silhouette. No secondary rounded layers are drawn inside it. */
    fun roundedRect(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color
    ) {
        if (width <= 0f || height <= 0f) return
        val r = radius.coerceAtMost(minOf(width, height) * 0.5f).coerceAtLeast(0f)
        shape.color = color
        if (r <= 0.5f) {
            shape.rect(x, y, width, height)
            return
        }
        shape.rect(x + r, y, width - 2f * r, height)
        shape.rect(x, y + r, r, height - 2f * r)
        shape.rect(x + width - r, y + r, r, height - 2f * r)
        shape.circle(x + r, y + r, r, 32)
        shape.circle(x + width - r, y + r, r, 32)
        shape.circle(x + r, y + height - r, r, 32)
        shape.circle(x + width - r, y + height - r, r, 32)
    }

    /** True vertical gradient rendered only inside a rounded silhouette. */
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
        if (width <= 0f || height <= 0f) return
        val r = radius.coerceAtMost(minOf(width, height) * 0.5f).coerceAtLeast(0f)
        val count = steps.coerceIn(4, 16)
        val bandHeight = height / count

        for (i in 0 until count) {
            val bandY = y + i * bandHeight
            val centerY = bandY + bandHeight * 0.5f
            val t = ((centerY - y) / height).coerceIn(0f, 1f)
            gradientColor.set(
                topColor.r + (bottomColor.r - topColor.r) * t,
                topColor.g + (bottomColor.g - topColor.g) * t,
                topColor.b + (bottomColor.b - topColor.b) * t,
                topColor.a + (bottomColor.a - topColor.a) * t
            )
            val distanceFromTop = centerY - y
            val distanceFromBottom = y + height - centerY
            val inset = when {
                distanceFromTop < r -> {
                    val d = r - distanceFromTop
                    r - kotlin.math.sqrt((r * r - d * d).coerceAtLeast(0f))
                }
                distanceFromBottom < r -> {
                    val d = r - distanceFromBottom
                    r - kotlin.math.sqrt((r * r - d * d).coerceAtLeast(0f))
                }
                else -> 0f
            }
            shape.color = gradientColor
            shape.rect(
                x + inset,
                bandY,
                (width - inset * 2f).coerceAtLeast(0f),
                bandHeight + 0.02f
            )
        }
    }

    /** Full-screen glass dimmer. */
    fun overlay(shape: ShapeRenderer, width: Float, height: Float) {
        shape.color = UiTheme.OVERLAY
        shape.rect(0f, 0f, width, height)
    }

    /** Optional atmospheric glow. Kept separate from cards so card silhouettes remain clean. */
    fun ambientGlow(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        accent: Color = UiTheme.CYAN
    ) {
        glowColor.set(accent.r, accent.g, accent.b, 0.035f)
        roundedRect(shape, x - 20f, y - 20f, width + 40f, height + 40f, radius + 20f, glowColor)
        glowColor.set(UiTheme.MAGENTA.r, UiTheme.MAGENTA.g, UiTheme.MAGENTA.b, 0.025f)
        roundedRect(shape, x - 9f, y - 9f, width + 18f, height + 18f, radius + 9f, glowColor)
    }

    /** One true rounded translucent surface. Decorative layers never create secondary circles. */
    fun glassPanel(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.PANEL_RADIUS
    ) {
        if (width <= 0f || height <= 0f) return
        val r = radius.coerceAtMost(minOf(width, height) * 0.5f)

        topColor.set(UiTheme.INNER)
        topColor.a = 0.58f
        roundedRect(shape, x, y, width, height, r, topColor)

        accentColor.set(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.20f)
        roundedRect(shape, x, y, width, height, r, accentColor)

        topColor.set(UiTheme.INNER)
        topColor.a = 0.48f
        roundedRect(
            shape,
            x + 2f,
            y + 2f,
            (width - 4f).coerceAtLeast(0f),
            (height - 4f).coerceAtLeast(0f),
            (r - 2f).coerceAtLeast(0f),
            topColor
        )

        // Only thin interior accents; no circles, halos, or secondary rounded surfaces.
        roundedRect(
            shape,
            x + 8f,
            y + height - 5f,
            (width - 16f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            UiTheme.GLASS_HIGHLIGHT
        )
    }

    /** One true rounded translucent card. */
    fun glassCard(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.CARD_RADIUS,
        accent: Color = UiTheme.CYAN
    ) {
        if (width <= 0f || height <= 0f) return
        val r = radius.coerceAtMost(minOf(width, height) * 0.5f)

        topColor.set(UiTheme.CARD)
        topColor.a = 0.56f
        roundedRect(shape, x, y, width, height, r, topColor)

        accentColor.set(accent.r, accent.g, accent.b, 0.16f)
        roundedRect(shape, x, y, width, height, r, accentColor)

        topColor.set(UiTheme.CARD)
        topColor.a = 0.48f
        roundedRect(
            shape,
            x + 2f,
            y + 2f,
            (width - 4f).coerceAtLeast(0f),
            (height - 4f).coerceAtLeast(0f),
            (r - 2f).coerceAtLeast(0f),
            topColor
        )

        accentColor.set(accent.r, accent.g, accent.b, 0.26f)
        roundedRect(
            shape,
            x + 3f,
            y + height - 4f,
            (width - 6f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            accentColor
        )
    }

    /** Rounded translucent action surface. */
    fun softButton(
        shape: ShapeRenderer,
        rect: Rectangle,
        color: Color,
        radius: Float = UiTheme.Metrics.BUTTON_RADIUS
    ) {
        if (rect.width <= 0f || rect.height <= 0f) return
        val r = radius.coerceAtMost(minOf(rect.width, rect.height) * 0.5f)

        topColor.set(color)
        topColor.a = 0.54f
        roundedRect(shape, rect.x, rect.y, rect.width, rect.height, r, topColor)

        accentColor.set(color.r, color.g, color.b, 0.22f)
        roundedRect(shape, rect.x, rect.y, rect.width, rect.height, r, accentColor)

        topColor.set(UiTheme.INNER)
        topColor.a = 0.34f
        roundedRect(
            shape,
            rect.x + 2f,
            rect.y + 2f,
            (rect.width - 4f).coerceAtLeast(0f),
            (rect.height - 4f).coerceAtLeast(0f),
            (r - 2f).coerceAtLeast(0f),
            topColor
        )

        accentColor.set(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.22f)
        roundedRect(
            shape,
            rect.x + 7f,
            rect.y + rect.height - 5f,
            (rect.width - 14f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            accentColor
        )
    }

    /** Circular frosted-glass touch control. */
    fun glassCircle(
        shape: ShapeRenderer,
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Color
    ) {
        // These are intentionally circular controls, separate from card rendering.
        accentColor.set(accent.r, accent.g, accent.b, 0.18f)
        shape.color = accentColor
        shape.circle(cx, cy, radius, 64)
        shape.color = UiTheme.TOUCH_BASE
        shape.circle(cx, cy, radius - 2f, 64)
        shape.color = UiTheme.TOUCH_INNER
        shape.circle(cx, cy, (radius - 10f).coerceAtLeast(2f), 64)
    }
}
