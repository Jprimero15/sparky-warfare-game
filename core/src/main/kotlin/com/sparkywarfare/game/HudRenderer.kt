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
        val scale = if (baseWidth > 0f) (maxWidth / baseWidth).coerceAtMost(preferred) else preferred
        font.data.setScale(scale.coerceAtLeast(minimum))
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
        shape.color = Color(0.55f, 0.9f, 1f, 0.55f)
        shape.rect(rect.x, rect.y + rect.height - 3f, rect.width, 3f)
    }
}
