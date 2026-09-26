package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

/** Reusable glassmorphic primitives for the Soft Neon Arcade UI. */
object UiShapes {
    /** Shared UI pass state so translucent glass actually blends over the game world. */
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
    private val bottomColor = Color()
    private val glowColor = Color()
    private val accentColor = Color()

    fun roundedRect(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color
    ) {
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

    /** True non-overlapping vertical gradient clipped to a rounded silhouette. */
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

            val inset = if (r <= 0.5f) {
                0f
            } else {
                val distanceFromTop = centerY - y
                val distanceFromBottom = y + height - centerY
                when {
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

    /** Soft atmospheric glow behind a surface. */
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

    /** Frosted glass panel: translucent fill, restrained glow, and a thin luminous edge. */
    fun glassPanel(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.PANEL_RADIUS
    ) {
        ambientGlow(shape, x, y, width, height, radius)

        // A restrained halo instead of a heavy neon slab.
        roundedRect(
            shape, x - 2.5f, y - 2.5f, width + 5f, height + 5f,
            radius + 2.5f, UiTheme.PANEL_EDGE
        )

        topColor.set(UiTheme.INNER).lerp(UiTheme.CYAN, 0.025f)
        topColor.a = 0.46f
        bottomColor.set(UiTheme.PANEL_DARK).lerp(UiTheme.MAGENTA, 0.025f)
        bottomColor.a = 0.54f
        gradientRoundedRect(shape, x, y, width, height, radius, topColor, bottomColor, 10)

        // Frosted top reflection and two tiny accent glints.
        roundedRect(
            shape, x + 3f, y + height - 4f, width - 6f, 1.5f, 0.75f,
            UiTheme.GLASS_HIGHLIGHT
        )
        roundedRect(
            shape, x + 7f, y + height - 7f, width * 0.34f, 2f, 1f,
            UiTheme.CYAN_SOFT
        )
        roundedRect(
            shape, x + width * 0.68f, y + 4f, width * 0.24f, 1.5f, 0.75f,
            UiTheme.MAGENTA_SOFT
        )
    }

    /** Frosted glass card used for stats, choices and secondary surfaces. */
    fun glassCard(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.CARD_RADIUS,
        accent: Color = UiTheme.CYAN
    ) {
        glowColor.set(accent.r, accent.g, accent.b, 0.025f)
        roundedRect(shape, x - 3f, y - 3f, width + 6f, height + 6f, radius + 3f, glowColor)

        topColor.set(UiTheme.CARD).lerp(accent, 0.035f)
        topColor.a = 0.43f
        bottomColor.set(UiTheme.PANEL_DARK).lerp(accent, 0.02f)
        bottomColor.a = 0.48f
        gradientRoundedRect(shape, x, y, width, height, radius, topColor, bottomColor, 8)

        accentColor.set(accent.r, accent.g, accent.b, 0.22f)
        roundedRect(shape, x + 2f, y + height - 3f, width - 4f, 1.5f, 0.75f, accentColor)
        roundedRect(shape, x + 6f, y + 5f, width - 12f, 1f, 0.5f, UiTheme.GLASS_HIGHLIGHT)
    }

    /** Translucent punchy button: glass body + restrained dual-neon edge. */
    fun softButton(
        shape: ShapeRenderer,
        rect: Rectangle,
        color: Color,
        radius: Float = UiTheme.Metrics.BUTTON_RADIUS
    ) {
        if (rect.width <= 0f || rect.height <= 0f) return

        glowColor.set(color.r, color.g, color.b, 0.035f)
        roundedRect(shape, rect.x - 5f, rect.y - 5f, rect.width + 10f, rect.height + 10f, radius + 5f, glowColor)

        val dark = color.r < 0.12f && color.g < 0.16f && color.b < 0.22f
        if (dark) {
            topColor.set(UiTheme.INNER).lerp(UiTheme.CYAN, 0.025f)
            topColor.a = 0.40f
            bottomColor.set(UiTheme.PANEL_DARK).lerp(UiTheme.MAGENTA, 0.025f)
            bottomColor.a = 0.50f
        } else {
            val alternate = if (color.g > color.r) UiTheme.MAGENTA else UiTheme.CYAN
            topColor.set(color).lerp(UiTheme.WHITE, 0.08f)
            topColor.a = 0.66f
            bottomColor.set(color).lerp(alternate, 0.24f)
            bottomColor.a = 0.58f
        }
        gradientRoundedRect(shape, rect.x, rect.y, rect.width, rect.height, radius, topColor, bottomColor, 10)

        // Thin edge accents; no thick sci-fi frame.
        accentColor.set(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, if (dark) 0.16f else 0.30f)
        roundedRect(shape, rect.x + 2f, rect.y + rect.height - 2.5f, rect.width * 0.52f, 1.5f, 0.75f, accentColor)
        accentColor.set(UiTheme.MAGENTA.r, UiTheme.MAGENTA.g, UiTheme.MAGENTA.b, 0.18f)
        roundedRect(shape, rect.x + rect.width * 0.66f, rect.y + 2f, rect.width * 0.30f, 1.5f, 0.75f, accentColor)
        roundedRect(shape, rect.x + 7f, rect.y + rect.height - 6f, rect.width * 0.40f, 1f, 0.5f, UiTheme.GLASS_HIGHLIGHT)
    }

    /** Circular frosted-glass touch control with a true neon rim, not a filled color disc. */
    fun glassCircle(
        shape: ShapeRenderer,
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Color
    ) {
        glowColor.set(accent.r, accent.g, accent.b, 0.035f)
        shape.color = glowColor
        shape.circle(cx, cy, radius + 11f, 56)

        accentColor.set(accent.r, accent.g, accent.b, 0.18f)
        shape.color = accentColor
        shape.circle(cx, cy, radius, 56)

        shape.color = UiTheme.TOUCH_BASE
        shape.circle(cx, cy, radius - 2f, 56)

        shape.color = UiTheme.TOUCH_INNER
        shape.circle(cx, cy, (radius - 10f).coerceAtLeast(2f), 56)

        accentColor.set(accent.r, accent.g, accent.b, 0.075f)
        shape.color = accentColor
        shape.circle(cx, cy, (radius - 13f).coerceAtLeast(2f), 56)

        roundedRect(
            shape,
            cx - radius * 0.42f,
            cy + radius * 0.52f,
            radius * 0.84f,
            2f,
            1f,
            UiTheme.GLASS_HIGHLIGHT
        )
    }
}
