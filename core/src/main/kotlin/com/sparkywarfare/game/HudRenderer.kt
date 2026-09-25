package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class HudRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val camera: OrthographicCamera
) {
    private val textWidths = HashMap<String, Float>()

    private fun widthAtBaseScale(text: String): Float = textWidths.getOrPut(text) {
        layout.setText(font, text)
        layout.width
    }

    fun fit(text: String, maxWidth: Float, preferred: Float, minimum: Float) {
        font.data.setScale(1f)
        val baseWidth = widthAtBaseScale(text)
        if (baseWidth <= 0f) {
            font.data.setScale(preferred)
            return
        }
        val widthScale = (maxWidth / baseWidth).coerceAtLeast(0.05f)
        val scale = minOf(preferred, maxOf(minimum, widthScale))
        font.data.setScale(minOf(scale, widthScale))
    }

    fun centered(text: String, x: Float, y: Float, color: Color) {
        val width = widthAtBaseScale(text) * font.data.scaleX
        font.color = color
        font.draw(batch, text, x - width / 2f, y)
    }

    fun right(text: String, x: Float, y: Float, color: Color) {
        val width = widthAtBaseScale(text) * font.data.scaleX
        font.color = color
        font.draw(batch, text, x - width, y)
    }

    fun shadowed(text: String, x: Float, y: Float, color: Color) {
        font.color = Color(0f, 0f, 0f, 0.8f)
        font.draw(batch, text, x + 2f, y - 2f)
        font.color = color
        font.draw(batch, text, x, y)
    }

    fun button(rect: Rectangle, color: Color) {
        shape.color = color
        shape.rect(rect.x, rect.y, rect.width, rect.height)
        shape.color = Color(0.18f, 0.9f, 1f, 0.68f)
        shape.rect(rect.x + 3f, rect.y + rect.height - 3f, rect.width - 6f, 3f)
        shape.color = Color(0.78f, 0.24f, 1f, 0.42f)
        shape.rect(rect.x + rect.width - 44f, rect.y + 3f, 41f, 2f)
    }
}
