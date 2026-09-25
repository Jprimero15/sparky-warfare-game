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
        shape.color = UiTheme.BLACK; shape.color.a = 0.74f
        shape.rect(0f, 0f, width, height)
        shape.color = UiTheme.PANEL
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = UiTheme.INNER
        shape.rect(panelX + 6f, panelY + 6f, panelW - 12f, panelH - 12f)
        shape.color = UiTheme.CYAN
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = UiTheme.MAGENTA
        shape.rect(panelX + panelW - 4f, panelY + panelH - 38f, 4f, 34f)
        shape.color = UiTheme.CYAN_SOFT
        shape.rect(split, panelY + 34f, 2f, panelH - 68f)
        shape.color = UiTheme.CYAN_SOFT
        shape.rect(panelX + 28f, panelY + panelH * 0.42f, split - panelX - 56f, 2f)
        shape.color = UiTheme.MAGENTA_SOFT
        shape.rect(split + 28f, panelY + 34f, panelX + panelW - split - 56f, 2f)

        drawButton(single, UiTheme.CYAN)
        drawButton(multi, UiTheme.PANEL_DARK)
        drawButton(settings, UiTheme.MAGENTA)
        shape.end()

        batch.begin()
        text.fit(titleFont, "SPARKY WARFARE", split - panelX - 72f, 1.55f, 0.9f)
        text.centered(titleFont, "SPARKY WARFARE", panelX + (split - panelX) / 2f, panelY + panelH - 72f, UiTheme.CYAN)
        text.fit(bodyFont, "CYBER COMBAT", split - panelX - 72f, 1.0f, 0.72f)
        text.centered(bodyFont, "CYBER COMBAT", panelX + (split - panelX) / 2f, panelY + panelH - 122f, UiTheme.MAGENTA)
        text.fit(bodyFont, "TACTICAL NETWORK // LOCAL", split - panelX - 72f, 0.72f, 0.52f)
        text.centered(bodyFont, "TACTICAL NETWORK // LOCAL", panelX + (split - panelX) / 2f, panelY + panelH - 151f, UiTheme.DIM)
        text.fit(bodyFont, "SYSTEM READY", split - panelX - 72f, 0.72f, 0.52f)
        text.centered(bodyFont, "SYSTEM READY", panelX + (split - panelX) / 2f, panelY + panelH * 0.47f, UiTheme.CYAN)
        text.fit(bodyFont, "BEST SCORE  $highScore", split - panelX - 72f, 0.68f, 0.48f)
        text.centered(bodyFont, "BEST SCORE  $highScore", panelX + (split - panelX) / 2f, panelY + 92f, UiTheme.WHITE)
        text.fit(bodyFont, "BEST WAVE  $bestWave", split - panelX - 72f, 0.62f, 0.44f)
        text.centered(bodyFont, "BEST WAVE  $bestWave", panelX + (split - panelX) / 2f, panelY + 66f, UiTheme.DIM)
        text.fit(bodyFont, "TOTAL KILLS  $totalKills", split - panelX - 72f, 0.62f, 0.44f)
        text.centered(bodyFont, "TOTAL KILLS  $totalKills", panelX + (split - panelX) / 2f, panelY + 40f, UiTheme.DIM)
        drawCommand(single, "DEPLOY", "START COMBAT", UiTheme.WHITE, UiTheme.CYAN)
        drawCommand(multi, "NETWORK", "MULTIPLAYER UNAVAILABLE", UiTheme.DIM, UiTheme.DIM_DARK)
        drawCommand(settings, "SYSTEM", "AUDIO // CONTROLS", UiTheme.WHITE, UiTheme.MAGENTA)
        text.fit(bodyFont, "BUILD 01  //  ANDROID", panelX + panelW - split - 70f, 0.56f, 0.4f)
        text.centered(bodyFont, "BUILD 01  //  ANDROID", split + (panelX + panelW - split) / 2f, panelY + 30f, UiTheme.DIM)
        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun drawCommand(rect: Rectangle, title: String, subtitle: String, titleColor: Color, subtitleColor: Color) {
        val cx = rect.x + rect.width / 2f
        text.fit(titleFont, title, rect.width - 46f, 0.82f, 0.56f)
        text.centered(titleFont, title, cx, rect.y + rect.height * 0.66f, titleColor)
        text.fit(bodyFont, subtitle, rect.width - 46f, 0.62f, 0.42f)
        text.centered(bodyFont, subtitle, cx, rect.y + rect.height * 0.25f, subtitleColor)
    }
}
