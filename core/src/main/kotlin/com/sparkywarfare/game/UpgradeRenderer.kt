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

        val wide = width >= height * 1.30f
        val gap = (if (wide) width * 0.018f else height * 0.018f).coerceIn(10f, 22f)
        val usableW = (panelW - 48f).coerceAtLeast(240f)
        val cardW = if (wide) {
            ((usableW - gap * 2f) / 3f).coerceIn(185f, 330f)
        } else {
            usableW
        }
        val cardH = if (wide) {
            (panelH * 0.52f).coerceIn(210f, 300f)
        } else {
            ((panelH - 150f - gap * 2f) / 3f).coerceIn(120f, 170f)
        }
        val totalW = if (wide) cardW * 3f + gap * 2f else cardW
        val left = cx - totalW / 2f
        val bottom = panelY + 30f

        UiShapes.begin(shape)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, panelX, panelY, panelW, panelH)

        for (i in 0 until 3) {
            val x = if (wide) left + i * (cardW + gap) else left
            val y = if (wide) bottom else bottom + (2 - i) * (cardH + gap)
            buttons[i].set(x, y, cardW, cardH)
            UiShapes.glassCard(
                shape,
                x,
                y,
                cardW,
                cardH,
                UiTheme.Metrics.CARD_RADIUS,
                if (i == 1) UiTheme.MAGENTA else UiTheme.CYAN
            )
            UiShapes.roundedRect(
                shape,
                x + 14f,
                y + cardH - 7f,
                cardW - 28f,
                3f,
                1.5f,
                if (i == 1) UiTheme.MAGENTA_SOFT else UiTheme.CYAN_SOFT
            )
        }
        UiShapes.end(shape)

        batch.begin()
        text.fitWithin(titleFont, "CHOOSE AN UPGRADE", panelW - 100f, 52f, 1.08f, 0.70f)
        text.centeredVertically(titleFont, "CHOOSE AN UPGRADE", cx, panelY + panelH - 56f, UiTheme.TEXT_PRIMARY)

        text.fitWithin(bodyFont, "WAVE  $wave  •  PICK ONE", panelW - 100f, 26f, 0.62f, 0.44f)
        text.centeredVertically(bodyFont, "WAVE  $wave  •  PICK ONE", cx, panelY + panelH - 92f, UiTheme.CYAN)

        for (i in 0 until minOf(3, choices.size)) {
            val r = buttons[i]
            val choice = choices[i]

            text.fitWithin(titleFont, choice.title, r.width - 34f, 44f, 0.74f, 0.50f)
            text.centeredVertically(titleFont, choice.title, r.x + r.width / 2f, r.y + r.height - 48f, UiTheme.TEXT_PRIMARY)

            text.fitWithin(bodyFont, choice.description, r.width - 36f, 54f, 0.62f, 0.44f)
            text.centeredVertically(bodyFont, choice.description, r.x + r.width / 2f, r.y + r.height * 0.54f, UiTheme.TEXT_SECONDARY)

            text.fitWithin(bodyFont, "INSTALL", r.width - 48f, 28f, 0.56f, 0.42f)
            text.centeredVertically(bodyFont, "INSTALL", r.x + r.width / 2f, r.y + 25f, if (i == 1) UiTheme.MAGENTA else UiTheme.CYAN)
        }

        text.reset(titleFont, bodyFont)
        batch.end()
    }
}
