package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class UpgradeRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont
) {
    private val panel = Color(0.006f, 0.018f, 0.032f, 0.98f)
    private val card = Color(0.015f, 0.04f, 0.065f, 0.98f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val dim = Color(0.42f, 0.62f, 0.69f, 1f)
    private val layout = com.badlogic.gdx.graphics.g2d.GlyphLayout()

    fun draw(
        width: Float,
        height: Float,
        wave: Int,
        choices: List<UpgradeType>,
        buttons: Array<Rectangle>,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Unit
    ) {
        val cx = width / 2f
        val availableWidth = width * 0.9f
        val gap = (width * 0.022f).coerceIn(12f, 28f)
        val cardW = ((availableWidth - gap * 2f) / 3f).coerceIn(180f, 320f)
        val cardH = (height * 0.48f).coerceIn(230f, 330f)
        val totalW = cardW * 3f + gap * 2f
        val left = cx - totalW / 2f
        val bottom = (height * 0.18f).coerceAtLeast(82f)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.88f)
        shape.rect(0f, 0f, width, height)
        shape.color = panel
        shape.rect(width * 0.04f, height * 0.06f, width * 0.92f, height * 0.88f)
        shape.color = cyan
        shape.rect(width * 0.04f, height * 0.94f - 3f, width * 0.92f, 3f)
        shape.color = magenta
        shape.rect(width * 0.96f - 4f, height * 0.94f - 34f, 4f, 31f)

        for (i in 0 until 3) {
            val x = left + i * (cardW + gap)
            buttons[i].set(x, bottom, cardW, cardH)
            shape.color = card
            shape.rect(x, bottom, cardW, cardH)
            shape.color = if (i == 1) magenta else cyan
            shape.rect(x, bottom + cardH - 4f, cardW, 4f)
            shape.color = Color(0.18f, 0.9f, 1f, 0.12f)
            shape.rect(x + 8f, bottom + 8f, 3f, cardH - 16f)
        }
        shape.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.begin()
        fitTitle("UPGRADE PROTOCOL", width * 0.72f, 1.18f, 0.76f)
        drawCentered("UPGRADE PROTOCOL", cx, height * 0.86f, cyan)
        fitBody("WAVE $wave COMPLETE // SELECT ONE SYSTEM MOD", width * 0.78f, 0.82f, 0.56f)
        bodyFont.color = dim
        bodyFont.draw(batch, "WAVE $wave COMPLETE // SELECT ONE SYSTEM MOD", cx - layout.width / 2f, height * 0.79f)

        for (i in 0 until minOf(3, choices.size)) {
            val r = buttons[i]
            val choice = choices[i]
            fitTitle(choice.title, r.width - 28f, 0.72f, 0.48f)
            drawCentered(choice.title, r.x + r.width / 2f, r.y + r.height - 56f, Color.WHITE)
            fitBody(choice.description, r.width - 28f, 0.78f, 0.52f)
            bodyFont.color = Color(0.58f, 0.8f, 0.88f, 1f)
            bodyFont.draw(batch, choice.description, r.x + r.width / 2f - layout.width / 2f, r.y + r.height / 2f + 5f)
            fitBody("TAP TO INSTALL", r.width - 30f, 0.62f, 0.44f)
            bodyFont.color = if (i == 1) magenta else cyan
            bodyFont.draw(batch, "TAP TO INSTALL", r.x + r.width / 2f - layout.width / 2f, r.y + 27f)
        }
        titleFont.data.setScale(1f)
        bodyFont.data.setScale(1f)
        batch.end()
    }
    private fun fitTitle(text: String, max: Float, preferred: Float, minimum: Float) {
        titleFont.data.setScale(1f)
        layout.setText(titleFont, text)
        val ws = if (layout.width > 0f) max / layout.width else preferred
        titleFont.data.setScale(minOf(preferred, ws.coerceAtLeast(minimum), ws))
    }

    private fun fitBody(text: String, max: Float, preferred: Float, minimum: Float) {
        bodyFont.data.setScale(1f)
        layout.setText(bodyFont, text)
        val ws = if (layout.width > 0f) max / layout.width else preferred
        bodyFont.data.setScale(minOf(preferred, ws.coerceAtLeast(minimum), ws))
        layout.setText(bodyFont, text)
    }
}
