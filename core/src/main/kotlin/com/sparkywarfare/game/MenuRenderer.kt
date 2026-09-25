package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MenuRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
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
        drawButton: (Rectangle, Color) -> Unit,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val cx = (safe.left + safe.right) / 2f
        val panelW = (width * 0.74f).coerceIn(430f, 720f)
        val panelH = (height * 0.82f).coerceIn(390f, 650f)
        val panelX = cx - panelW / 2f
        val panelY = (safe.bottom + safe.top) / 2f - panelH / 2f

        // Darkened battlefield with a focused command-console panel.
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.78f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.008f, 0.025f, 0.035f, 0.96f)
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = Color(0.08f, 0.55f, 0.7f, 0.12f)
        shape.rect(panelX + 2f, panelY + 2f, panelW - 4f, panelH - 4f)
        shape.color = Color(0.25f, 0.85f, 1f, 0.85f)
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = Color(0.25f, 0.85f, 1f, 0.22f)
        shape.rect(panelX, panelY, 4f, panelH)

        // Decorative console marks.
        shape.color = Color(0.25f, 0.85f, 1f, 0.2f)
        shape.rect(panelX + 24f, panelY + panelH - 32f, 76f, 2f)
        shape.rect(panelX + panelW - 100f, panelY + panelH - 32f, 76f, 2f)

        drawButton(single, Color(0.02f, 0.34f, 0.5f, 0.96f))
        drawButton(multi, Color(0.035f, 0.055f, 0.07f, 0.96f))
        drawButton(settings, Color(0.025f, 0.15f, 0.19f, 0.98f))
        shape.end()

        batch.begin()
        fit("SPARKY WARFARE", panelW - 50f, 2.5f, 1.4f)
        drawCentered("SPARKY WARFARE", cx, panelY + panelH - 62f, Color(0.62f, 0.94f, 1f, 1f))
        font.data.setScale(0.72f)
        drawCentered("TACTICAL ENERGY COMBAT", cx, panelY + panelH - 92f, Color(0.45f, 0.62f, 0.69f, 1f))

        font.data.setScale(0.62f)
        drawCentered("COMMAND CONSOLE  //  READY", cx, panelY + panelH - 124f, Color(0.3f, 0.75f, 0.82f, 1f))

        font.data.setScale(0.72f)
        drawCentered("SINGLE PLAYER", single.x + single.width / 2f, single.y + single.height / 2f + 9f, Color.WHITE)
        font.data.setScale(0.58f)
        drawCentered("START A NEW RUN", single.x + single.width / 2f, single.y + 17f, Color(0.66f, 0.86f, 0.91f, 1f))

        font.data.setScale(0.66f)
        drawCentered("MULTIPLAYER", multi.x + multi.width / 2f, multi.y + multi.height / 2f + 9f, Color(0.68f, 0.76f, 0.8f, 1f))
        font.data.setScale(0.5f)
        drawCentered("COMING SOON", multi.x + multi.width / 2f, multi.y + 17f, Color(0.38f, 0.48f, 0.52f, 1f))

        font.data.setScale(0.66f)
        drawCentered("SETTINGS", settings.x + settings.width / 2f, settings.y + settings.height / 2f + 8f, Color(0.78f, 0.94f, 0.96f, 1f))

        val statsY = panelY + 42f
        font.data.setScale(0.52f)
        drawCentered("BEST SCORE  $highScore    •    BEST WAVE  $bestWave    •    KILLS  $totalKills", cx, statsY, Color(0.42f, 0.58f, 0.63f, 1f))
        font.data.setScale(0.46f)
        drawCentered("ANDROID  //  SINGLE-PLAYER BUILD", cx, panelY + 21f, Color(0.26f, 0.4f, 0.44f, 1f))
        batch.end()
    }
}
