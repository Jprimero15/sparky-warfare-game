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
        fit: (String, Float, Float, Float) -> Float
    ) {
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.8f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.012f, 0.035f, 0.05f, 0.98f)
        shape.rect(width * 0.2f, height * 0.17f, width * 0.6f, height * 0.66f)
        shape.color = Color(0.18f, 0.78f, 1f, 0.82f)
        shape.rect(width * 0.2f, height * 0.83f - 4f, width * 0.6f, 4f)
        drawButton(resume, Color(0.02f, 0.34f, 0.49f, 0.96f))
        drawButton(menu, Color(0.04f, 0.065f, 0.085f, 0.96f))
        shape.end()

        batch.begin()
        fit("PAUSED", width * 0.42f, 0.98f, 0.76f)
        drawCentered("PAUSED", width / 2f, height * 0.68f, Color(0.58f, 0.94f, 1f, 1f))
        fit("RESUME", resume.width - 18f, 0.66f, 0.5f)
        drawCentered("RESUME", resume.x + resume.width / 2f, resume.y + resume.height / 2f + 8f, Color.WHITE)
        fit("MAIN MENU", menu.width - 18f, 0.66f, 0.5f)
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.78f, 0.87f, 0.91f, 1f))
        font.data.setScale(1f)
        batch.end()
    }
}
