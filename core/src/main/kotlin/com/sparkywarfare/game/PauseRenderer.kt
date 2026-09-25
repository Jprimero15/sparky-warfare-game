package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class PauseRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {
    private val panel = Color(0.006f, 0.016f, 0.028f, 0.98f)
    private val inner = Color(0.018f, 0.038f, 0.06f, 0.97f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val dim = Color(0.4f, 0.58f, 0.66f, 1f)

    fun draw(
        width: Float,
        height: Float,
        resume: Rectangle,
        menu: Rectangle,
        drawButton: (Rectangle, Color) -> Unit,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val panelW = (width * 0.62f).coerceIn(380f, 740f)
        val panelH = (height * 0.68f).coerceIn(300f, 500f)
        val panelX = width / 2f - panelW / 2f
        val panelY = height / 2f - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.78f)
        shape.rect(0f, 0f, width, height)
        shape.color = panel
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = inner
        shape.rect(panelX + 6f, panelY + 6f, panelW - 12f, panelH - 12f)
        shape.color = cyan
        shape.rect(panelX, panelY + panelH - 3f, panelW, 3f)
        shape.color = magenta
        shape.rect(panelX + panelW - 4f, panelY + panelH - 34f, 4f, 31f)
        shape.color = Color(0.18f, 0.9f, 1f, 0.18f)
        shape.rect(panelX + 26f, panelY + panelH - 112f, panelW - 52f, 2f)
        drawButton(resume, Color(0.02f, 0.22f, 0.34f, 0.98f))
        drawButton(menu, Color(0.045f, 0.055f, 0.10f, 0.98f))
        shape.end()

        batch.begin()
        val cx = width / 2f
        fit("COMBAT SUSPENDED", panelW - 70f, 1.26f, 0.78f)
        drawCentered("COMBAT SUSPENDED", cx, panelY + panelH - 58f, cyan)
        fit("SYSTEM HOLD // INPUT LOCKED", panelW - 90f, 0.46f, 0.34f)
        drawCentered("SYSTEM HOLD // INPUT LOCKED", cx, panelY + panelH - 91f, magenta)
        drawCommand(resume, "RESUME", "RETURN TO BATTLE", Color(0.92f, 0.98f, 1f, 1f), cyan, drawCentered, fit)
        drawCommand(menu, "ABORT", "RETURN TO MAIN CONSOLE", Color(0.82f, 0.86f, 0.92f, 1f), dim, drawCentered, fit)
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
        val cx = rect.x + rect.width / 2f
        fit(title, rect.width - 44f, 0.76f, 0.58f)
        centered(title, cx, rect.y + rect.height * 0.62f, titleColor)
        fit(subtitle, rect.width - 44f, 0.36f, 0.28f)
        centered(subtitle, cx, rect.y + rect.height * 0.22f, subtitleColor)
    }
}
