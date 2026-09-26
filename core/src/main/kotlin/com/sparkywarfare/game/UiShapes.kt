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

    /** Translucent rounded glass panel with no square halo outside its silhouette. */
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
        val rim = Color(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.15f)
        roundedRect(shape, x, y, width, height, r, rim)

        topColor.set(UiTheme.INNER)
        topColor.a = 0.44f
        roundedRect(
            shape,
            x + 1.5f,
            y + 1.5f,
            (width - 3f).coerceAtLeast(0f),
            (height - 3f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            topColor
        )

        bottomColor.set(UiTheme.PANEL_DARK)
        bottomColor.a = 0.46f
        roundedRect(
            shape,
            x + 1.5f,
            y + height * 0.42f,
            (width - 3f).coerceAtLeast(0f),
            (height * 0.58f - 1.5f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            bottomColor
        )

        roundedRect(
            shape,
            x + 7f,
            y + height - 6f,
            (width - 14f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            UiTheme.GLASS_HIGHLIGHT
        )
        roundedRect(
            shape,
            x + 9f,
            y + height - 9f,
            (width * 0.30f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            UiTheme.CYAN_SOFT
        )
    }

    /** Translucent rounded card; neighboring cards never bleed into its silhouette. */
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
        accentColor.set(accent.r, accent.g, accent.b, 0.16f)
        roundedRect(shape, x, y, width, height, r, accentColor)

        topColor.set(UiTheme.CARD)
        topColor.a = 0.34f
        roundedRect(
            shape,
            x + 1.5f,
            y + 1.5f,
            (width - 3f).coerceAtLeast(0f),
            (height - 3f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            topColor
        )

        bottomColor.set(UiTheme.PANEL_DARK)
        bottomColor.a = 0.38f
        roundedRect(
            shape,
            x + 1.5f,
            y + height * 0.45f,
            (width - 3f).coerceAtLeast(0f),
            (height * 0.55f - 1.5f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            bottomColor
        )

        accentColor.set(accent.r, accent.g, accent.b, 0.26f)
        roundedRect(
            shape,
            x + 2f,
            y + height - 3f,
            (width - 4f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            accentColor
        )
        roundedRect(
            shape,
            x + 7f,
            y + height - 7f,
            (width * 0.34f).coerceAtLeast(0f),
            1f,
            0.5f,
            UiTheme.GLASS_HIGHLIGHT
        )
    }

    /** Rounded translucent action surface without an exterior glow halo. */
    fun softButton(
        shape: ShapeRenderer,
        rect: Rectangle,
        color: Color,
        radius: Float = UiTheme.Metrics.BUTTON_RADIUS
    ) {
        if (rect.width <= 0f || rect.height <= 0f) return
        val r = radius.coerceAtMost(minOf(rect.width, rect.height) * 0.5f)
        val dark = color.r < 0.12f && color.g < 0.16f && color.b < 0.22f

        accentColor.set(color.r, color.g, color.b, if (dark) 0.16f else 0.24f)
        roundedRect(shape, rect.x, rect.y, rect.width, rect.height, r, accentColor)

        topColor.set(if (dark) UiTheme.INNER else color).lerp(UiTheme.WHITE, if (dark) 0.02f else 0.06f)
        topColor.a = if (dark) 0.38f else 0.52f
        roundedRect(
            shape,
            rect.x + 1.5f,
            rect.y + 1.5f,
            (rect.width - 3f).coerceAtLeast(0f),
            (rect.height - 3f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            topColor
        )

        bottomColor.set(if (dark) UiTheme.PANEL_DARK else color)
            .lerp(if (dark) UiTheme.MAGENTA else UiTheme.CYAN, if (dark) 0.02f else 0.20f)
        bottomColor.a = if (dark) 0.46f else 0.48f
        roundedRect(
            shape,
            rect.x + 1.5f,
            rect.y + rect.height * 0.45f,
            (rect.width - 3f).coerceAtLeast(0f),
            (rect.height * 0.55f - 1.5f).coerceAtLeast(0f),
            (r - 1.5f).coerceAtLeast(0f),
            bottomColor
        )

        accentColor.set(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, if (dark) 0.14f else 0.28f)
        roundedRect(
            shape,
            rect.x + 2f,
            rect.y + rect.height - 2.5f,
            (rect.width * 0.52f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            accentColor
        )
        accentColor.set(UiTheme.MAGENTA.r, UiTheme.MAGENTA.g, UiTheme.MAGENTA.b, 0.18f)
        roundedRect(
            shape,
            rect.x + rect.width * 0.66f,
            rect.y + 2f,
            (rect.width * 0.30f).coerceAtLeast(0f),
            1.5f,
            0.75f,
            accentColor
        )
        roundedRect(
            shape,
            rect.x + 7f,
            rect.y + rect.height - 6f,
            (rect.width * 0.40f).coerceAtLeast(0f),
            1f,
            0.5f,
            UiTheme.GLASS_HIGHLIGHT
        )
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
