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
        fit: (String, Float, Float, Float) -> Unit
    ) {
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.9f)
        shape.rect(0f, 0f, width, height)
        val panelX = width * 0.14f
        val panelY = height * 0.20f
        val panelW = width * 0.72f
        val panelH = height * 0.60f
        shape.color = Color(0.035f, 0.012f, 0.02f, 0.98f)
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = Color(1f, 0.22f, 0.3f, 0.82f)
        shape.rect(panelX, panelY + panelH - 4f, panelW, 4f)
        drawButton(retry, Color(0.02f, 0.32f, 0.46f, 0.92f))
        drawButton(menu, Color(0.04f, 0.06f, 0.08f, 0.94f))
        shape.end()

        batch.begin()
        fit("RUN TERMINATED", width * 0.52f, 2f, 1.15f)
        drawCentered("RUN TERMINATED", width / 2f, panelY + panelH - 48f, Color(1f, 0.35f, 0.4f, 1f))
        font.data.setScale(0.62f)
        drawCentered("COMBAT SESSION ENDED", width / 2f, panelY + panelH - 78f, Color(0.5f, 0.62f, 0.66f, 1f))
        font.data.setScale(0.72f)
        drawCentered("SCORE  $score     BEST  $best", width / 2f, panelY + panelH - 132f, Color.WHITE)
        font.data.setScale(0.58f)
        drawCentered("WAVE  $wave    KILLS  $kills    COMBO  x$combo", width / 2f, panelY + panelH - 164f, Color(0.58f, 0.72f, 0.76f, 1f))
        font.data.setScale(0.68f)
        drawCentered("RETRY", retry.x + retry.width / 2f, retry.y + retry.height / 2f + 8f, Color.WHITE)
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.78f, 0.86f, 0.9f, 1f))
        batch.end()
    }
}
