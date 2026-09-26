package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class PauseRenderer(
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
        resume: Rectangle,
        menu: Rectangle,
        drawButton: (Rectangle, Color) -> Unit
    ) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = safe.right - safe.left
        val sh = safe.top - safe.bottom
        val panelW = minOf(sw * 0.66f, 820f).coerceAtLeast(340f)
        val panelH = minOf(sh * 0.68f, 440f).coerceAtLeast(250f)
        val x = cx - panelW / 2f
        val y = cy - panelH / 2f

        UiShapes.begin(shape)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, x, y, panelW, panelH)
        drawButton(resume, UiTheme.CYAN)
        drawButton(menu, UiTheme.PANEL_DARK)
        UiShapes.end(shape)

        batch.begin()
        text.fitWithin(titleFont, "PAUSED", panelW - 90f, 62f, 1.2f, 0.74f)
        text.centered(titleFont, "PAUSED", cx, y + panelH - 64f, UiTheme.TEXT_PRIMARY)

        text.fitWithin(bodyFont, "TAKE A BREATH", panelW - 100f, 28f, 0.65f, 0.46f)
        text.centered(bodyFont, "TAKE A BREATH", cx, y + panelH - 100f, UiTheme.CYAN)

        command(resume, "RESUME", UiTheme.TEXT_PRIMARY)
        command(menu, "MAIN MENU", UiTheme.TEXT_PRIMARY)
        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun command(rect: Rectangle, title: String, color: Color) {
        val cx = rect.x + rect.width / 2f
        text.fitWithin(titleFont, title, rect.width - 52f, rect.height * 0.44f, 0.82f, 0.56f)
        val h = text.height(titleFont, title)
        text.centered(titleFont, title, cx, rect.y + rect.height / 2f + h / 2f, color)
    }
}
