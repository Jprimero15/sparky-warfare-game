package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MenuRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
) {
    fun draw(
        width: Float,
        height: Float,
        safe: SafeArea,
        single: Rectangle,
        settings: Rectangle,
        highScore: Int,
        bestWave: Int,
        totalKills: Int,
        drawButton: (Rectangle, Color) -> Unit
    ) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = (safe.right - safe.left).coerceAtLeast(1f)
        val sh = (safe.top - safe.bottom).coerceAtLeast(1f)
        val wide = sw >= sh * 1.30f

        // Keep the visual surface driven by the same geometry used for touch input.
        // No decorative cards sit behind buttons or text.
        val panelW = (if (wide) sw * 0.88f else sw * 0.90f).coerceAtMost(if (wide) 1180f else 760f)
            .coerceAtMost(sw - 24f).coerceAtLeast(280f)
        val panelH = (if (wide) sh * 0.86f else sh * 0.90f).coerceAtMost(if (wide) 640f else 760f)
            .coerceAtMost(sh - 24f).coerceAtLeast(260f)
        val panelX = cx - panelW / 2f
        val panelY = cy - panelH / 2f

        val labels = arrayOf("BEST SCORE", "BEST WAVE", "TOTAL KILLS")
        val values = arrayOf(highScore.toString(), bestWave.toString(), totalKills.toString())

        UiShapes.begin(shape)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, panelX, panelY, panelW, panelH)
        drawButton(single, UiTheme.CYAN)
        drawButton(settings, UiTheme.PANEL_DARK)

        if (wide) {
            val splitX = panelX + panelW * 0.50f
            val statX = panelX + 34f
            val statW = splitX - statX - 28f
            val statH = 50f
            val statGap = 10f
            val statY = panelY + 34f
            for (i in 0 until 3) {
                UiShapes.glassCard(
                    shape, statX, statY + i * (statH + statGap), statW, statH,
                    16f, if (i == 0) UiTheme.CYAN else UiTheme.MAGENTA
                )
            }
        } else {
            val statY = panelY + 22f
            val gap = 8f
            val statW = ((panelW - 44f - gap * 2f) / 3f).coerceAtLeast(72f)
            val statH = 42f
            for (i in 0 until 3) {
                UiShapes.glassCard(
                    shape, panelX + 22f + i * (statW + gap), statY, statW, statH,
                    14f, if (i == 0) UiTheme.CYAN else UiTheme.MAGENTA
                )
            }
        }
        UiShapes.end(shape)

        batch.begin()

        if (wide) {
            val leftCenter = panelX + panelW * 0.25f
            text.fitWithin(titleFont, "SPARKY WARFARE", panelW * 0.43f, 62f, 1.30f, 0.78f)
            text.centeredVertically(titleFont, "SPARKY WARFARE", leftCenter, panelY + panelH - 58f, UiTheme.TEXT_PRIMARY)

            text.fitWithin(bodyFont, "SOFT NEON COMBAT", panelW * 0.40f, 28f, 0.70f, 0.48f)
            text.centeredVertically(bodyFont, "SOFT NEON COMBAT", leftCenter, panelY + panelH - 98f, UiTheme.CYAN)

            text.fitWithin(bodyFont, "SYSTEM READY", panelW * 0.40f, 24f, 0.56f, 0.42f)
            text.centeredVertically(bodyFont, "SYSTEM READY", leftCenter, panelY + panelH * 0.52f, UiTheme.TEXT_SECONDARY)

            val statX = panelX + 34f
            val splitX = panelX + panelW * 0.50f
            val statW = splitX - statX - 28f
            val statH = 50f
            val statGap = 10f
            val statY = panelY + 34f
            for (i in 0 until 3) {
                val sy = statY + i * (statH + statGap)
                text.fitWithin(bodyFont, labels[i], statW * 0.58f, 20f, 0.50f, 0.38f)
                text.centeredVertically(bodyFont, labels[i], statX + statW * 0.32f, sy + statH / 2f, UiTheme.TEXT_SECONDARY)
                text.fitWithin(titleFont, values[i], statW * 0.26f, 26f, 0.68f, 0.46f)
                text.centeredVertically(titleFont, values[i], statX + statW * 0.88f, sy + statH / 2f, UiTheme.TEXT_PRIMARY)
            }
        } else {
            text.fitWithin(titleFont, "SPARKY WARFARE", panelW - 44f, 48f, 1.02f, 0.64f)
            text.centeredVertically(titleFont, "SPARKY WARFARE", cx, panelY + panelH - 46f, UiTheme.TEXT_PRIMARY)

            text.fitWithin(bodyFont, "SOFT NEON COMBAT", panelW - 56f, 24f, 0.58f, 0.42f)
            text.centeredVertically(bodyFont, "SOFT NEON COMBAT", cx, panelY + panelH - 78f, UiTheme.CYAN)

            val statY = panelY + 22f
            val gap = 8f
            val statW = ((panelW - 44f - gap * 2f) / 3f).coerceAtLeast(72f)
            val statH = 42f
            for (i in 0 until 3) {
                val sx = panelX + 22f + i * (statW + gap)
                text.fitWithin(bodyFont, labels[i], statW - 8f, 14f, 0.38f, 0.30f)
                text.centeredVertically(bodyFont, labels[i], sx + statW / 2f, statY + 27f, UiTheme.TEXT_SECONDARY)
                text.fitWithin(titleFont, values[i], statW - 8f, 18f, 0.50f, 0.36f)
                text.centeredVertically(titleFont, values[i], sx + statW / 2f, statY + 12f, UiTheme.TEXT_PRIMARY)
            }
        }

        command(single, "SINGLE PLAYER", UiTheme.TEXT_PRIMARY)
        command(settings, "SETTINGS", UiTheme.TEXT_PRIMARY)

        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun command(rect: Rectangle, title: String, color: Color) {
        val cx = rect.x + rect.width / 2f
        text.fitWithin(
            titleFont,
            title,
            rect.width - 54f,
            rect.height * 0.42f,
            UiTheme.Metrics.BUTTON_TEXT_SCALE,
            UiTheme.Metrics.BUTTON_TEXT_MIN_SCALE
        )
        text.centeredVertically(titleFont, title, cx, rect.y + rect.height / 2f, color)
    }
}
