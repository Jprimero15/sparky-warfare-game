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
        val safeHeight = (safe.top - safe.bottom).coerceAtLeast(1f)
        val stackTop = single.y + single.height
        val stackBottom = settings.y
        val panelW = (safe.right - safe.left).coerceIn(320f, 720f) * 0.80f
        val panelX = cx - panelW / 2f
        val panelY = (safe.bottom + safe.top) / 2f - (safeHeight * 0.43f).coerceAtMost(330f)
        val panelH = ((safeHeight * 0.86f).coerceAtMost(660f)).coerceAtLeast(300f)

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.78f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.006f, 0.018f, 0.027f, 0.97f)
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = Color(0.03f, 0.09f, 0.12f, 0.9f)
        shape.rect(panelX + 5f, panelY + 5f, panelW - 10f, panelH - 10f)
        shape.color = Color(0.25f, 0.85f, 1f, 0.9f)
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = Color(0.25f, 0.85f, 1f, 0.22f)
        shape.rect(panelX, panelY, 3f, panelH)

        drawButton(single, Color(0.02f, 0.36f, 0.53f, 0.98f))
        drawButton(multi, Color(0.02f, 0.055f, 0.075f, 0.98f))
        drawButton(settings, Color(0.025f, 0.13f, 0.17f, 0.98f))
        shape.end()

        batch.begin()
        fit("SPARKY WARFARE", panelW - 48f, 2.5f, 1.35f)
        drawCentered("SPARKY WARFARE", cx, panelY + panelH - 52f, Color(0.65f, 0.95f, 1f, 1f))
        font.data.setScale(0.68f)
        drawCentered("TACTICAL ENERGY COMBAT", cx, panelY + panelH - 82f, Color(0.45f, 0.62f, 0.69f, 1f))

        font.data.setScale(0.48f)
        drawCentered("ONLINE COMBAT  //  OFFLINE BUILD READY", cx, panelY + panelH - 112f, Color(0.3f, 0.7f, 0.76f, 1f))

        font.data.setScale(0.72f)
        drawCentered("SINGLE PLAYER", single.x + single.width / 2f, single.y + single.height * 0.60f, Color.WHITE)
        font.data.setScale(0.48f)
        drawCentered("START NEW RUN", single.x + single.width / 2f, single.y + 14f, Color(0.64f, 0.84f, 0.9f, 1f))

        font.data.setScale(0.64f)
        drawCentered("MULTIPLAYER", multi.x + multi.width / 2f, multi.y + multi.height * 0.60f, Color(0.62f, 0.72f, 0.76f, 1f))
        font.data.setScale(0.46f)
        drawCentered("NOT AVAILABLE YET", multi.x + multi.width / 2f, multi.y + 14f, Color(0.36f, 0.47f, 0.51f, 1f))

        font.data.setScale(0.64f)
        drawCentered("SETTINGS", settings.x + settings.width / 2f, settings.y + settings.height * 0.60f, Color(0.78f, 0.94f, 0.96f, 1f))

        // Small lower status strip inside the console.
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0.12f, 0.46f, 0.55f, 0.18f)
        shape.rect(panelX + 20f, panelY + 54f, panelW - 40f, 1.5f)
        shape.end()

        font.data.setScale(0.5f)
        drawCentered("BEST SCORE  $highScore   •   WAVE  $bestWave   •   KILLS  $totalKills", cx, panelY + 36f, Color(0.42f, 0.58f, 0.63f, 1f))
        font.data.setScale(0.42f)
        drawCentered("ANDROID // SINGLE-PLAYER", cx, panelY + 18f, Color(0.26f, 0.4f, 0.44f, 1f))
        batch.end()
    }
}
