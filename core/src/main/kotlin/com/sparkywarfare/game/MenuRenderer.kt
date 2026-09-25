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
        val panelW = minOf(sw * 0.92f, 1220f).coerceAtLeast(280f)
        val panelH = minOf(sh * 0.90f, 680f).coerceAtLeast(250f)
        val panelX = cx - panelW / 2f
        val panelY = cy - panelH / 2f
        val split = panelX + panelW * 0.54f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.overlay(shape, width, height)
        UiShapes.panel(shape, panelX, panelY, panelW, panelH)

        drawButton(single, UiTheme.CYAN)
        drawButton(multi, UiTheme.PANEL_DARK)
        drawButton(settings, UiTheme.MAGENTA)
        shape.end()

        batch.begin()
        text.fit(titleFont, "SPARKY WARFARE", split - panelX - 72f, 1.55f, 0.9f)
        text.centered(titleFont, "SPARKY WARFARE", panelX + (split - panelX) / 2f, panelY + panelH - 72f, UiTheme.CYAN)
        text.fit(bodyFont, "READY", split - panelX - 72f, 0.72f, 0.52f)
        text.centered(bodyFont, "READY", panelX + (split - panelX) / 2f, panelY + panelH * 0.47f, UiTheme.CYAN)
        text.fit(bodyFont, "BEST SCORE  $highScore", split - panelX - 72f, 0.68f, 0.48f)
        text.centered(bodyFont, "BEST SCORE  $highScore", panelX + (split - panelX) / 2f, panelY + 92f, UiTheme.TEXT_PRIMARY)
        text.fit(bodyFont, "BEST WAVE  $bestWave", split - panelX - 72f, 0.62f, 0.44f)
        text.centered(bodyFont, "BEST WAVE  $bestWave", panelX + (split - panelX) / 2f, panelY + 66f, UiTheme.TEXT_SECONDARY)
        text.fit(bodyFont, "TOTAL KILLS  $totalKills", split - panelX - 72f, 0.62f, 0.44f)
        text.centered(bodyFont, "TOTAL KILLS  $totalKills", panelX + (split - panelX) / 2f, panelY + 40f, UiTheme.TEXT_SECONDARY)
        drawCommand(single, "PLAY", UiTheme.TEXT_PRIMARY)
        drawCommand(multi, "MULTIPLAYER", UiTheme.TEXT_SECONDARY)
        drawCommand(settings, "SETTINGS", UiTheme.TEXT_PRIMARY)
        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun drawCommand(rect: Rectangle, title: String, color: Color) {
        val cx = rect.x + rect.width / 2f
        text.fitWithin(titleFont, title, rect.width - 46f, rect.height * 0.50f, 0.9f, 0.58f)
        val titleHeight = text.height(titleFont, title)
        text.centered(titleFont, title, cx, rect.y + rect.height / 2f + titleHeight / 2f, color)
    }
}
