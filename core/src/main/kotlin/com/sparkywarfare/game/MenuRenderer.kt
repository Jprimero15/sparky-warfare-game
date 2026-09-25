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
        multi: Rectangle,
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
        val panelW = minOf(sw * 0.88f, 1180f).coerceAtLeast(520f)
        val panelH = minOf(sh * 0.86f, 640f).coerceAtLeast(300f)
        val panelX = cx - panelW / 2f
        val panelY = cy - panelH / 2f
        val splitX = panelX + panelW * 0.50f
        val leftW = splitX - panelX

        shape.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.overlay(shape, width, height)

        // Layered glass cards create the soft depth seen in the reference style.
        UiShapes.glassCard(
            shape, panelX - 34f, panelY + 34f, panelW * 0.38f, panelH * 0.78f,
            UiTheme.Metrics.CARD_RADIUS, UiTheme.MAGENTA
        )
        UiShapes.glassCard(
            shape, panelX + 24f, panelY - 26f, panelW * 0.34f, panelH * 0.72f,
            UiTheme.Metrics.CARD_RADIUS, UiTheme.CYAN
        )
        UiShapes.glassPanel(shape, panelX, panelY, panelW, panelH)

        val statX = panelX + 38f
        val statW = leftW - 76f
        val statGap = 10f
        val statH = 54f
        val statY = panelY + 34f
        for (i in 0 until 3) {
            UiShapes.glassCard(
                shape,
                statX,
                statY + i * (statH + statGap),
                statW,
                statH,
                18f,
                if (i == 0) UiTheme.CYAN else UiTheme.MAGENTA
            )
        }

        drawButton(single, UiTheme.CYAN)
        drawButton(multi, UiTheme.MAGENTA)
        drawButton(settings, UiTheme.PANEL_DARK)
        shape.end()

        batch.begin()

        text.fitWithin(titleFont, "SPARKY WARFARE", leftW - 70f, 72f, 1.42f, 0.86f)
        text.centered(
            titleFont, "SPARKY WARFARE",
            panelX + leftW / 2f,
            panelY + panelH - 66f,
            UiTheme.TEXT_PRIMARY
        )

        text.fitWithin(bodyFont, "SOFT NEON COMBAT", leftW - 70f, 30f, 0.76f, 0.52f)
        text.centered(
            bodyFont, "SOFT NEON COMBAT",
            panelX + leftW / 2f,
            panelY + panelH - 104f,
            UiTheme.CYAN
        )

        text.fitWithin(bodyFont, "SYSTEM READY", 180f, 28f, 0.65f, 0.48f)
        text.centered(
            bodyFont,
            "SYSTEM READY",
            panelX + leftW * 0.50f,
            panelY + panelH * 0.50f,
            UiTheme.TEXT_SECONDARY
        )

        val labels = arrayOf("BEST SCORE", "BEST WAVE", "TOTAL KILLS")
        val values = arrayOf(highScore.toString(), bestWave.toString(), totalKills.toString())
        for (i in 0 until 3) {
            val sy = statY + i * (statH + statGap)
            text.fitWithin(bodyFont, labels[i], statW * 0.58f, 22f, 0.55f, 0.40f)
            bodyFont.color = UiTheme.TEXT_SECONDARY
            bodyFont.draw(batch, labels[i], statX + 16f, sy + 34f)
            text.fitWithin(titleFont, values[i], statW * 0.28f, 28f, 0.72f, 0.50f)
            text.right(titleFont, values[i], statX + statW - 16f, sy + 32f, UiTheme.TEXT_PRIMARY)
        }

        command(single, "SINGLE PLAYER", UiTheme.TEXT_PRIMARY)
        command(multi, "MULTIPLAYER", UiTheme.TEXT_PRIMARY)
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
        val titleHeight = text.height(titleFont, title)
        text.centered(titleFont, title, cx, rect.y + rect.height / 2f + titleHeight / 2f, color)
    }
}
