package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class UpgradeRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
) {
    fun draw(
        width: Float,
        height: Float,
        wave: Int,
        choices: List<UpgradeType>,
        buttons: Array<Rectangle>
    ) {
        val cx = width / 2f
        val panelW = (width * 0.90f).coerceAtMost(1120f)
        val panelH = (height * 0.86f).coerceAtMost(620f)
        val panelX = cx - panelW / 2f
        val panelY = height / 2f - panelH / 2f

        val gap = (width * 0.018f).coerceIn(12f, 24f)
        val cardW = ((panelW - 56f - gap * 2f) / 3f).coerceIn(185f, 330f)
        val cardH = (panelH * 0.52f).coerceIn(210f, 300f)
        val totalW = cardW * 3f + gap * 2f
        val left = cx - totalW / 2f
        val bottom = panelY + 48f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, panelX, panelY, panelW, panelH)

        for (i in 0 until 3) {
            val x = left + i * (cardW + gap)
            buttons[i].set(x, bottom, cardW, cardH)
            UiShapes.glassCard(
                shape,
                x,
                bottom,
                cardW,
                cardH,
                UiTheme.Metrics.CARD_RADIUS,
                if (i == 1) UiTheme.MAGENTA else UiTheme.CYAN
            )
            UiShapes.roundedRect(
                shape,
                x + 14f,
                bottom + cardH - 7f,
                cardW - 28f,
                3f,
                1.5f,
                if (i == 1) UiTheme.MAGENTA_SOFT else UiTheme.CYAN_SOFT
            )
        }
        shape.end()

        batch.begin()
        text.fitWithin(titleFont, "CHOOSE AN UPGRADE", panelW - 100f, 52f, 1.08f, 0.70f)
        text.centered(titleFont, "CHOOSE AN UPGRADE", cx, panelY + panelH - 56f, UiTheme.TEXT_PRIMARY)

        text.fitWithin(bodyFont, "WAVE  $wave  •  PICK ONE", panelW - 100f, 26f, 0.62f, 0.44f)
        text.centered(bodyFont, "WAVE  $wave  •  PICK ONE", cx, panelY + panelH - 92f, UiTheme.CYAN)

        for (i in 0 until minOf(3, choices.size)) {
            val r = buttons[i]
            val choice = choices[i]

            text.fitWithin(titleFont, choice.title, r.width - 34f, 44f, 0.74f, 0.50f)
            text.centered(titleFont, choice.title, r.x + r.width / 2f, r.y + r.height - 54f, UiTheme.TEXT_PRIMARY)

            text.fitWithin(bodyFont, choice.description, r.width - 36f, 54f, 0.62f, 0.44f)
            text.centered(bodyFont, choice.description, r.x + r.width / 2f, r.y + r.height * 0.54f, UiTheme.TEXT_SECONDARY)

            text.fitWithin(bodyFont, "INSTALL", r.width - 48f, 28f, 0.56f, 0.42f)
            text.centered(bodyFont, "INSTALL", r.x + r.width / 2f, r.y + 28f, if (i == 1) UiTheme.MAGENTA else UiTheme.CYAN)
        }

        text.reset(titleFont, bodyFont)
        batch.end()
    }
}
