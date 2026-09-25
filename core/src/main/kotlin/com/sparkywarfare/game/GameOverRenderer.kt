package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class GameOverRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {
    private val panel = Color(0.028f, 0.008f, 0.018f, 0.98f)
    private val inner = Color(0.055f, 0.018f, 0.035f, 0.96f)
    private val danger = Color(1f, 0.2f, 0.34f, 1f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val dim = Color(0.52f, 0.64f, 0.7f, 1f)

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
        drawButton: (Rectangle, Color) -> Unit,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val cx = (safe.left + safe.right) / 2f
        val panelW = (safe.right - safe.left).coerceIn(380f, 920f) * 0.88f
        val panelH = (safe.top - safe.bottom).coerceIn(340f, 520f) * 0.82f
        val panelX = cx - panelW / 2f
        val panelY = (safe.bottom + safe.top) / 2f - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.88f)
        shape.rect(0f, 0f, width, height)
        shape.color = panel
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = inner
        shape.rect(panelX + 6f, panelY + 6f, panelW - 12f, panelH - 12f)
        shape.color = danger
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = Color(1f, 0.2f, 0.34f, 0.2f)
        shape.rect(panelX + 26f, panelY + panelH - 126f, panelW - 52f, 2f)
        shape.color = cyan
        shape.rect(panelX, panelY + 4f, panelW * 0.36f, 3f)
        drawButton(retry, Color(0.02f, 0.22f, 0.34f, 0.98f))
        drawButton(menu, Color(0.06f, 0.05f, 0.1f, 0.98f))
        shape.end()

        batch.begin()
        fit("SYSTEM FAILURE", panelW - 70f, 1.3f, 0.82f)
        drawCentered("SYSTEM FAILURE", cx, panelY + panelH - 58f, danger)
        fit("COMBAT SESSION TERMINATED", panelW - 90f, 0.5f, 0.36f)
        drawCentered("COMBAT SESSION TERMINATED", cx, panelY + panelH - 91f, dim)

        fit("SCORE  $score      BEST  $best", panelW - 76f, 0.68f, 0.5f)
        drawCentered("SCORE  $score      BEST  $best", cx, panelY + panelH - 141f, Color.WHITE)

        fit("WAVE  $wave    KILLS  $kills    COMBO  x$combo", panelW - 78f, 0.48f, 0.35f)
        drawCentered("WAVE  $wave    KILLS  $kills    COMBO  x$combo", cx, panelY + panelH - 174f, Color(0.64f, 0.76f, 0.81f, 1f))

        drawCommand(retry, "REDEPLOY", "START ANOTHER RUN", Color(0.92f, 0.98f, 1f, 1f), cyan, drawCentered, fit)
        drawCommand(menu, "MAIN CONSOLE", "RETURN TO MENU", Color(0.86f, 0.9f, 0.96f, 1f), dim, drawCentered, fit)
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
        fit(title, rect.width - 42f, 0.72f, 0.52f)
        centered(title, cx, rect.y + rect.height * 0.62f, titleColor)
        fit(subtitle, rect.width - 42f, 0.36f, 0.28f)
        centered(subtitle, cx, rect.y + rect.height * 0.22f, subtitleColor)
    }
}
