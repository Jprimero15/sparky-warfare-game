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
    private val bg = Color(0.004f, 0.008f, 0.014f, 0.96f)
    private val panel = Color(0.008f, 0.02f, 0.032f, 0.97f)
    private val panelInner = Color(0.012f, 0.035f, 0.052f, 0.96f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val cyanSoft = Color(0.18f, 0.9f, 1f, 0.24f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val magentaSoft = Color(0.78f, 0.24f, 1f, 0.28f)
    private val dim = Color(0.38f, 0.56f, 0.64f, 1f)
    private val white = Color(0.92f, 0.98f, 1f, 1f)

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
        val safeW = (safe.right - safe.left).coerceAtLeast(320f)
        val panelW = safeW.coerceIn(360f, 920f) * 0.88f
        val panelX = cx - panelW / 2f
        val panelTop = safe.top - 10f
        val panelBottom = safe.bottom + 10f
        val panelH = (panelTop - panelBottom).coerceAtLeast(320f)

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.76f)
        shape.rect(0f, 0f, width, height)

        shape.color = panel
        shape.rect(panelX, panelBottom, panelW, panelH)
        shape.color = panelInner
        shape.rect(panelX + 6f, panelBottom + 6f, panelW - 12f, panelH - 12f)

        // Cyber frame.
        shape.color = cyan
        shape.rect(panelX, panelTop - 3f, panelW, 3f)
        shape.color = magenta
        shape.rect(panelX + panelW - 4f, panelTop - 34f, 4f, 31f)
        shape.color = cyanSoft
        shape.rect(panelX, panelBottom + 3f, 3f, panelH - 6f)

        val scanY = panelTop - 146f
        shape.color = cyanSoft
        shape.rect(panelX + 28f, scanY, panelW - 56f, 2f)
        shape.color = magentaSoft
        shape.rect(panelX + 28f, panelBottom + 68f, panelW - 56f, 2f)

        drawButton(single, Color(0.02f, 0.22f, 0.34f, 0.98f))
        drawButton(multi, Color(0.025f, 0.05f, 0.075f, 0.98f))
        drawButton(settings, Color(0.065f, 0.08f, 0.16f, 0.98f))
        shape.end()

        batch.begin()
        fit("SPARKY WARFARE", panelW - 78f, 1.48f, 0.86f)
        drawCentered("SPARKY WARFARE", cx, panelTop - 62f, cyan)

        fit("CYBER COMBAT // TACTICAL NETWORK", panelW - 104f, 0.52f, 0.36f)
        drawCentered("CYBER COMBAT // TACTICAL NETWORK", cx, panelTop - 101f, dim)

        fit("SYSTEM STATUS  //  READY", panelW - 150f, 0.44f, 0.32f)
        drawCentered("SYSTEM STATUS  //  READY", cx, panelTop - 128f, magenta)

        drawCommand(single, "DEPLOY", "START NEW COMBAT RUN", white, cyan, drawCentered, fit)
        drawCommand(multi, "NETWORK", "MULTIPLAYER // OFFLINE", Color(0.56f, 0.68f, 0.73f, 1f), dim, drawCentered, fit)
        drawCommand(settings, "SYSTEM", "AUDIO / CONTROLS / DEVICE", Color(0.86f, 0.9f, 1f, 1f), magenta, drawCentered, fit)

        fit("BEST  $highScore    //    WAVE  $bestWave    //    KILLS  $totalKills",
            panelW - 70f, 0.46f, 0.34f)
        drawCentered(
            "BEST  $highScore    //    WAVE  $bestWave    //    KILLS  $totalKills",
            cx, panelBottom + 46f, dim
        )
        fit("TACTICAL CONSOLE  //  BUILD 01", panelW - 100f, 0.34f, 0.26f)
        drawCentered("TACTICAL CONSOLE  //  BUILD 01", cx, panelBottom + 22f, Color(0.25f, 0.42f, 0.5f, 1f))
        font.data.setScale(1f)
        batch.end()
    }

    private fun drawCommand(
        rect: Rectangle,
        title: String,
        subtitle: String,
        titleColor: Color,
        subtitleColor: Color,
        centered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val centerX = rect.x + rect.width / 2f
        fit(title, rect.width - 42f, 0.76f, 0.58f)
        centered(title, centerX, rect.y + rect.height * 0.62f, titleColor)
        fit(subtitle, rect.width - 42f, 0.39f, 0.29f)
        centered(subtitle, centerX, rect.y + rect.height * 0.24f, subtitleColor)
    }
}
