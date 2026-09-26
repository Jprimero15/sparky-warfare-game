package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class GameOverRenderer(
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
        retry: Rectangle,
        menu: Rectangle,
        score: Int,
        best: Int,
        wave: Int,
        kills: Int,
        combo: Int,
        drawButton: (Rectangle, Color) -> Unit
    ) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = safe.right - safe.left
        val sh = safe.top - safe.bottom
        val panelW = minOf(sw * 0.72f, 880f).coerceAtLeast(360f)
        val panelH = minOf(sh * 0.76f, 500f).coerceAtLeast(280f)
        val x = cx - panelW / 2f
        val y = cy - panelH / 2f

        UiShapes.begin(shape)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, x, y, panelW, panelH)
        UiShapes.glassCard(shape, x + 26f, y + panelH - 170f, panelW * 0.43f, 76f, 20f, UiTheme.CYAN)
        UiShapes.glassCard(shape, x + panelW * 0.50f, y + panelH - 170f, panelW * 0.43f, 76f, 20f, UiTheme.MAGENTA)
        drawButton(retry, UiTheme.CYAN)
        drawButton(menu, UiTheme.PANEL_DARK)
        UiShapes.end(shape)

        batch.begin()
        text.fitWithin(titleFont, "RUN OVER", panelW - 80f, 58f, 1.18f, 0.76f)
        text.centeredVertically(titleFont, "RUN OVER", cx, y + panelH - 58f, UiTheme.MAGENTA)

        text.fitWithin(bodyFont, "SCORE  $score", panelW * 0.36f, 30f, 0.72f, 0.48f)
        text.centeredVertically(bodyFont, "SCORE  $score", x + panelW * 0.25f, y + panelH - 135f, UiTheme.TEXT_PRIMARY)
        text.fitWithin(bodyFont, "BEST  $best", panelW * 0.36f, 30f, 0.72f, 0.48f)
        text.centeredVertically(bodyFont, "BEST  $best", x + panelW * 0.75f, y + panelH - 135f, UiTheme.TEXT_PRIMARY)

        val detail = "WAVE  $wave    •    KILLS  $kills    •    COMBO  x$combo"
        text.fitWithin(bodyFont, detail, panelW - 72f, 28f, 0.64f, 0.42f)
        text.centeredVertically(bodyFont, detail, cx, y + panelH - 195f, UiTheme.TEXT_SECONDARY)

        command(retry, "REDEPLOY", UiTheme.TEXT_PRIMARY)
        command(menu, "MAIN MENU", UiTheme.TEXT_PRIMARY)

        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun command(rect: Rectangle, title: String, color: Color) {
        val cx = rect.x + rect.width / 2f
        text.fitWithin(titleFont, title, rect.width - 52f, rect.height * 0.44f, 0.80f, 0.54f)
        text.centeredVertically(titleFont, title, cx, rect.y + rect.height / 2f, color)
    }
}
