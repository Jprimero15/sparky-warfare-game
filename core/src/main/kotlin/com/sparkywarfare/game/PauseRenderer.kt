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
    fun draw(
        width: Float,
        height: Float,
        resume: Rectangle,
        menu: Rectangle,
        drawButton: (Rectangle, Color) -> Unit,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Unit
    ) {
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.78f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.015f, 0.035f, 0.05f, 0.96f)
        shape.rect(width * 0.2f, height * 0.18f, width * 0.6f, height * 0.64f)
        drawButton(resume, Color(0.02f, 0.32f, 0.46f, 0.92f))
        drawButton(menu, Color(0.04f, 0.06f, 0.08f, 0.94f))
        shape.end()

        batch.begin()
        fit("PAUSED", width * 0.45f, 2f, 1.2f)
        drawCentered("PAUSED", width / 2f, height * 0.65f, Color(0.55f, 0.92f, 1f, 1f))
        font.data.setScale(0.7f)
        drawCentered("RESUME", resume.x + resume.width / 2f, resume.y + resume.height / 2f + 8f, Color.WHITE)
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.75f, 0.85f, 0.9f, 1f))
        batch.end()
    }
}
