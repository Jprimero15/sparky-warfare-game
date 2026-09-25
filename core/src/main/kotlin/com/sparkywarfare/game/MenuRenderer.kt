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
        val panelW = (safe.right - safe.left).coerceIn(340f, 860f) * 0.86f
        val panelX = cx - panelW / 2f
        val panelTop = safe.top - 10f
        val panelBottom = safe.bottom + 10f
        val panelH = (panelTop - panelBottom).coerceAtLeast(300f)

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.82f)
        shape.rect(0f, 0f, width, height)

        // Punchy sci-fi command panel: dark core, cyan edge, secondary scan line.
        shape.color = Color(0.006f, 0.018f, 0.027f, 0.98f)
        shape.rect(panelX, panelBottom, panelW, panelH)
        shape.color = Color(0.018f, 0.065f, 0.085f, 0.98f)
        shape.rect(panelX + 4f, panelBottom + 4f, panelW - 8f, panelH - 8f)
        shape.color = Color(0.18f, 0.78f, 1f, 0.92f)
        shape.rect(panelX, panelTop - 4f, panelW, 4f)
        shape.color = Color(0.18f, 0.78f, 1f, 0.25f)
        shape.rect(panelX, panelBottom, 3f, panelH)
        shape.color = Color(1f, 0.25f, 0.34f, 0.72f)
        shape.rect(panelX + panelW - 3f, panelTop - 32f, 3f, 28f)

        drawButton(single, Color(0.015f, 0.38f, 0.58f, 0.98f))
        drawButton(multi, Color(0.02f, 0.06f, 0.08f, 0.98f))
        drawButton(settings, Color(0.025f, 0.15f, 0.19f, 0.98f))

        shape.color = Color(0.16f, 0.72f, 0.86f, 0.24f)
        shape.rect(panelX + 22f, panelBottom + 54f, panelW - 44f, 2f)
        shape.end()

        batch.begin()

        // Dedicated vertical lanes prevent the large title, subtitle and status
        // copy from ever sharing the same baseline band.
        fit("SPARKY WARFARE", panelW - 70f, 1.15f, 0.82f)
        drawCentered("SPARKY WARFARE", cx, panelTop - 58f, Color(0.68f, 0.96f, 1f, 1f))

        fit("TACTICAL ENERGY COMBAT", panelW - 90f, 0.58f, 0.46f)
        drawCentered("TACTICAL ENERGY COMBAT", cx, panelTop - 102f, Color(0.42f, 0.67f, 0.74f, 1f))

        fit("COMMAND CONSOLE // READY", panelW - 100f, 0.46f, 0.36f)
        drawCentered("COMMAND CONSOLE // READY", cx, panelTop - 132f, Color(0.3f, 0.72f, 0.78f, 1f))

        fit("SINGLE PLAYER", single.width - 34f, 0.72f, 0.58f)
        drawCentered("SINGLE PLAYER", single.x + single.width / 2f, single.y + single.height * 0.60f, Color.WHITE)
        fit("START NEW RUN", single.width - 34f, 0.42f, 0.34f)
        drawCentered("START NEW RUN", single.x + single.width / 2f, single.y + 12f, Color(0.68f, 0.88f, 0.94f, 1f))

        fit("MULTIPLAYER", multi.width - 34f, 0.62f, 0.52f)
        drawCentered("MULTIPLAYER", multi.x + multi.width / 2f, multi.y + multi.height * 0.60f, Color(0.62f, 0.75f, 0.8f, 1f))
        fit("NOT AVAILABLE YET", multi.width - 34f, 0.40f, 0.32f)
        drawCentered("NOT AVAILABLE YET", multi.x + multi.width / 2f, multi.y + 12f, Color(0.38f, 0.5f, 0.54f, 1f))

        fit("SETTINGS", settings.width - 34f, 0.62f, 0.52f)
        drawCentered("SETTINGS", settings.x + settings.width / 2f, settings.y + settings.height * 0.60f, Color(0.82f, 0.96f, 0.98f, 1f))

        fit("BEST $highScore   //   WAVE $bestWave   //   KILLS $totalKills", panelW - 48f, 0.44f, 0.34f)
        drawCentered("BEST $highScore   //   WAVE $bestWave   //   KILLS $totalKills", cx, panelBottom + 34f, Color(0.46f, 0.64f, 0.69f, 1f))
        fit("ANDROID  //  SINGLE-PLAYER", panelW - 80f, 0.34f, 0.28f)
        drawCentered("ANDROID  //  SINGLE-PLAYER", cx, panelBottom + 15f, Color(0.26f, 0.42f, 0.46f, 1f))

        font.data.setScale(1f)
        batch.end()
    }
}
