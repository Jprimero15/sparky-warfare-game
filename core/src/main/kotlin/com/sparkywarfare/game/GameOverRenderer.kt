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
        val panelW = (safe.right - safe.left).coerceIn(360f, 900f) * 0.86f
        val panelH = (safe.top - safe.bottom).coerceIn(320f, 460f) * 0.82f
        val panelX = cx - panelW / 2f
        val panelY = (safe.bottom + safe.top) / 2f - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.9f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.035f, 0.008f, 0.018f, 0.98f)
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = Color(1f, 0.2f, 0.3f, 0.88f)
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        shape.color = Color(1f, 0.2f, 0.3f, 0.18f)
        shape.rect(panelX + 8f, panelY + 8f, panelW - 16f, 2f)
        drawButton(retry, Color(0.02f, 0.34f, 0.49f, 0.96f))
        drawButton(menu, Color(0.04f, 0.065f, 0.085f, 0.96f))
        shape.end()

        batch.begin()
        fit("RUN TERMINATED", panelW - 80f, 1.02f, 0.78f)
        drawCentered("RUN TERMINATED", cx, panelY + panelH - 58f, Color(1f, 0.34f, 0.42f, 1f))

        fit("COMBAT SESSION ENDED", panelW - 110f, 0.52f, 0.40f)
        drawCentered("COMBAT SESSION ENDED", cx, panelY + panelH - 100f, Color(0.52f, 0.66f, 0.7f, 1f))

        fit("SCORE  $score     BEST  $best", panelW - 90f, 0.66f, 0.50f)
        drawCentered("SCORE  $score     BEST  $best", cx, panelY + panelH - 145f, Color.WHITE)

        fit("WAVE  $wave    KILLS  $kills    COMBO  x$combo", panelW - 90f, 0.50f, 0.38f)
        drawCentered("WAVE  $wave    KILLS  $kills    COMBO  x$combo", cx, panelY + panelH - 176f, Color(0.6f, 0.74f, 0.78f, 1f))

        fit("RETRY", retry.width - 20f, 0.66f, 0.50f)
        drawCentered("RETRY", retry.x + retry.width / 2f, retry.y + retry.height / 2f + 8f, Color.WHITE)
        fit("MAIN MENU", menu.width - 20f, 0.66f, 0.50f)
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.82f, 0.9f, 0.94f, 1f))

        font.data.setScale(1f)
        batch.end()
    }
}
