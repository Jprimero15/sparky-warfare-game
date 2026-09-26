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

    /** Smooth vertical gradient with the rounded silhouette retained. */
    fun gradientRoundedRect(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        topColor: Color,
        bottomColor: Color,
        steps: Int = 8
    ) {
        if (width <= 0f || height <= 0f) return
        val count = steps.coerceIn(4, 20)
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
            roundedRect(shape, x, bandY, width, bandHeight + 1.4f, radius, gradientColor)
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

    /** Glass panel: translucent depth, soft bloom, luminous edge, and inner highlight. */
    fun glassPanel(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.PANEL_RADIUS
    ) {
        ambientGlow(shape, x, y, width, height, radius)
        roundedRect(shape, x - 5f, y - 5f, width + 10f, height + 10f, radius + 5f, UiTheme.CYAN_GLOW)
        roundedRect(shape, x - 2f, y - 2f, width + 4f, height + 4f, radius + 2f, UiTheme.PANEL_EDGE)

        topColor.set(UiTheme.INNER).lerp(UiTheme.CYAN, 0.035f)
        bottomColor.set(UiTheme.PANEL_DARK).lerp(UiTheme.MAGENTA, 0.035f)
        gradientRoundedRect(shape, x, y, width, height, radius, topColor, bottomColor, 9)

        val inset = UiTheme.Metrics.PANEL_INSET
        roundedRect(
            shape,
            x + inset,
            y + inset,
            (width - inset * 2f).coerceAtLeast(0f),
            (height - inset * 2f).coerceAtLeast(0f),
            (radius - inset).coerceAtLeast(0f),
            UiTheme.GLASS_SHADOW
        )
        roundedRect(
            shape,
            x + 2f,
            y + height - 5f,
            width - 4f,
            2f,
            1f,
            UiTheme.GLASS_HIGHLIGHT
        )
        roundedRect(
            shape,
            x + 5f,
            y + height - 8f,
            width * 0.40f,
            3f,
            1.5f,
            UiTheme.CYAN_SOFT
        )
        roundedRect(
            shape,
            x + width * 0.60f,
            y + 5f,
            width * 0.35f,
            2f,
            1f,
            UiTheme.MAGENTA_SOFT
        )
    }

    /** Smaller glass card used for stats and upgrade choices. */
    fun glassCard(
        shape: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float = UiTheme.Metrics.CARD_RADIUS,
        accent: Color = UiTheme.CYAN
    ) {
        glowColor.set(accent.r, accent.g, accent.b, 0.045f)
        roundedRect(shape, x - 4f, y - 4f, width + 8f, height + 8f, radius + 4f, glowColor)
        topColor.set(UiTheme.CARD).lerp(accent, 0.06f)
        topColor.a = 0.78f
        bottomColor.set(UiTheme.PANEL_DARK).lerp(UiTheme.MAGENTA, 0.04f)
        bottomColor.a = 0.76f
        gradientRoundedRect(shape, x, y, width, height, radius, topColor, bottomColor, 7)
        accentColor.set(accent.r, accent.g, accent.b, 0.30f)
        roundedRect(shape, x + 1.5f, y + height - 3.5f, width - 3f, 2f, 1f, accentColor)
        roundedRect(shape, x + 4f, y + 4f, width - 8f, 1.5f, 0.75f, UiTheme.GLASS_HIGHLIGHT)
    }

    /** Glass button with dark translucent fill and cyan-to-violet edge glow. */
    fun softButton(
        shape: ShapeRenderer,
        rect: Rectangle,
        color: Color,
        radius: Float = UiTheme.Metrics.BUTTON_RADIUS
    ) {
        if (rect.width <= 0f || rect.height <= 0f) return

        val dark = color.r < 0.12f && color.g < 0.16f && color.b < 0.22f
        glowColor.set(color.r, color.g, color.b, if (dark) 0.045f else 0.10f)
        roundedRect(shape, rect.x - 7f, rect.y - 7f, rect.width + 14f, rect.height + 14f, radius + 7f, glowColor)
        glowColor.set(UiTheme.MAGENTA.r, UiTheme.MAGENTA.g, UiTheme.MAGENTA.b, if (dark) 0.035f else 0.065f)
        roundedRect(shape, rect.x - 3f, rect.y - 3f, rect.width + 6f, rect.height + 6f, radius + 3f, glowColor)

        if (dark) {
            topColor.set(UiTheme.INNER).lerp(UiTheme.CYAN, 0.035f)
            topColor.a = 0.84f
            bottomColor.set(UiTheme.PANEL_DARK).lerp(UiTheme.MAGENTA, 0.045f)
            bottomColor.a = 0.90f
        } else {
            val alternate = if (color.r > color.b) UiTheme.CYAN else UiTheme.MAGENTA
            topColor.set(color).lerp(UiTheme.WHITE, 0.14f)
            topColor.a = 0.94f
            bottomColor.set(color).lerp(alternate, 0.34f)
            bottomColor.a = 0.90f
        }
        gradientRoundedRect(shape, rect.x, rect.y, rect.width, rect.height, radius, topColor, bottomColor, 10)

        // Thin luminous edge accents, intentionally softer than a hard sci-fi console.
        accentColor.set(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.26f)
        roundedRect(shape, rect.x + 2f, rect.y + rect.height - 3f, rect.width * 0.56f, 2f, 1f, accentColor)
        accentColor.set(UiTheme.MAGENTA.r, UiTheme.MAGENTA.g, UiTheme.MAGENTA.b, 0.22f)
        roundedRect(shape, rect.x + rect.width * 0.58f, rect.y + 2f, rect.width * 0.40f, 2f, 1f, accentColor)
        roundedRect(shape, rect.x + 6f, rect.y + rect.height - 8f, rect.width - 12f, 2f, 1f, UiTheme.GLASS_HIGHLIGHT)
    }

    /** Circular glass touch control with a soft neon rim. */
    fun glassCircle(
        shape: ShapeRenderer,
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Color
    ) {
        glowColor.set(accent.r, accent.g, accent.b, 0.045f)
        shape.color = glowColor
        shape.circle(cx, cy, radius + 12f, 56)
        shape.color = UiTheme.TOUCH_BASE
        shape.circle(cx, cy, radius, 56)
        shape.color = UiTheme.TOUCH_INNER
        shape.circle(cx, cy, radius - 10f, 56)
        accentColor.set(accent.r, accent.g, accent.b, 0.24f)
        shape.color = accentColor
        shape.circle(cx, cy, radius - 3f, 56)
    }
}
