package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MenuRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont
) {
    private val panel = Color(0.006f, 0.016f, 0.028f, 0.98f)
    private val inner = Color(0.012f, 0.032f, 0.05f, 0.98f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val cyanSoft = Color(0.18f, 0.9f, 1f, 0.22f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val dim = Color(0.46f, 0.64f, 0.7f, 1f)
    private val white = Color(0.94f, 0.98f, 1f, 1f)
    private val layout = GlyphLayout()

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
        shape.color = Color(0f, 0f, 0f, 0.74f)
        shape.rect(0f, 0f, width, height)
        shape.color = panel
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = inner
        shape.rect(panelX + 6f, panelY + 6f, panelW - 12f, panelH - 12f)
        shape.color = cyan
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = magenta
        shape.rect(panelX + panelW - 4f, panelY + panelH - 38f, 4f, 34f)
        shape.color = cyanSoft
        shape.rect(split, panelY + 34f, 2f, panelH - 68f)
        shape.color = cyanSoft
        shape.rect(panelX + 28f, panelY + panelH * 0.42f, split - panelX - 56f, 2f)
        shape.color = Color(0.78f, 0.24f, 1f, 0.16f)
        shape.rect(split + 28f, panelY + 34f, panelX + panelW - split - 56f, 2f)

        drawButton(single, Color(0.02f, 0.22f, 0.34f, 0.98f))
        drawButton(multi, Color(0.025f, 0.055f, 0.085f, 0.98f))
        drawButton(settings, Color(0.06f, 0.06f, 0.14f, 0.98f))
        shape.end()

        batch.begin()
        titleFont.data.setScale(1f)
        fit(titleFont, "SPARKY WARFARE", split - panelX - 72f, 1.55f, 0.9f)
        centered(titleFont, "SPARKY WARFARE", panelX + (split - panelX) / 2f, panelY + panelH - 72f, cyan)

        fit(bodyFont, "CYBER COMBAT", split - panelX - 72f, 1.0f, 0.72f)
        centered(bodyFont, "CYBER COMBAT", panelX + (split - panelX) / 2f, panelY + panelH - 122f, magenta)

        fit(bodyFont, "TACTICAL NETWORK // LOCAL", split - panelX - 72f, 0.72f, 0.52f)
        centered(bodyFont, "TACTICAL NETWORK // LOCAL", panelX + (split - panelX) / 2f, panelY + panelH - 151f, dim)

        fit(bodyFont, "SYSTEM READY", split - panelX - 72f, 0.72f, 0.52f)
        centered(bodyFont, "SYSTEM READY", panelX + (split - panelX) / 2f, panelY + panelH * 0.47f, cyan)

        fit(bodyFont, "BEST SCORE  $highScore", split - panelX - 72f, 0.68f, 0.48f)
        centered(bodyFont, "BEST SCORE  $highScore", panelX + (split - panelX) / 2f, panelY + 92f, white)
        fit(bodyFont, "BEST WAVE  $bestWave", split - panelX - 72f, 0.62f, 0.44f)
        centered(bodyFont, "BEST WAVE  $bestWave", panelX + (split - panelX) / 2f, panelY + 66f, dim)
        fit(bodyFont, "TOTAL KILLS  $totalKills", split - panelX - 72f, 0.62f, 0.44f)
        centered(bodyFont, "TOTAL KILLS  $totalKills", panelX + (split - panelX) / 2f, panelY + 40f, dim)

        drawCommand(single, "DEPLOY", "START COMBAT", white, cyan)
        drawCommand(multi, "NETWORK", "MULTIPLAYER UNAVAILABLE", Color(0.7f, 0.8f, 0.84f, 1f), dim)
        drawCommand(settings, "SYSTEM", "AUDIO // CONTROLS", white, magenta)

        fit(bodyFont, "BUILD 01  //  ANDROID", panelX + panelW - split - 70f, 0.56f, 0.4f)
        centered(bodyFont, "BUILD 01  //  ANDROID", split + (panelX + panelW - split) / 2f, panelY + 30f, dim)
        reset()
        batch.end()
    }

    private fun drawCommand(rect: Rectangle, title: String, subtitle: String, titleColor: Color, subtitleColor: Color) {
        val cx = rect.x + rect.width / 2f
        fit(titleFont, title, rect.width - 46f, 0.82f, 0.56f)
        centered(titleFont, title, cx, rect.y + rect.height * 0.66f, titleColor)
        fit(bodyFont, subtitle, rect.width - 46f, 0.62f, 0.42f)
        centered(bodyFont, subtitle, cx, rect.y + rect.height * 0.25f, subtitleColor)
    }

    private fun fit(font: BitmapFont, text: String, maxWidth: Float, preferred: Float, minimum: Float) {
        font.data.setScale(1f)
        layout.setText(font, text)
        val widthScale = if (layout.width > 0f) maxWidth / layout.width else preferred
        font.data.setScale(minOf(preferred, widthScale.coerceAtLeast(minimum), widthScale))
    }

    private fun centered(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width / 2f, y)
    }

    private fun reset() {
        titleFont.data.setScale(1f)
        bodyFont.data.setScale(1f)
    }
}
