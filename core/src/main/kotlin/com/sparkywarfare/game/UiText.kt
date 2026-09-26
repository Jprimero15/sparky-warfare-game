package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/** Shared text fitting/drawing primitives used by every UI renderer. */
class UiText(private val batch: SpriteBatch) {
    private val layout = GlyphLayout()

    fun fit(font: BitmapFont, text: String, maxWidth: Float, preferred: Float, minimum: Float) {
        fitWithin(font, text, maxWidth, Float.POSITIVE_INFINITY, preferred, minimum)
    }

    fun fitWithin(
        font: BitmapFont,
        text: String,
        maxWidth: Float,
        maxHeight: Float,
        preferred: Float,
        minimum: Float
    ) {
        font.data.setScale(1f)
        layout.setText(font, text)
        if (layout.width <= 0f || layout.height <= 0f) {
            font.data.setScale(preferred.coerceAtLeast(0.05f))
            return
        }
        val widthScale = (maxWidth / layout.width).coerceAtLeast(0.05f)
        val heightScale = if (maxHeight.isFinite()) {
            (maxHeight / layout.height).coerceAtLeast(0.05f)
        } else {
            Float.POSITIVE_INFINITY
        }
        // Width and height constraints both win. This prevents glyphs from escaping
        // or vertically colliding inside compact landscape controls.
        val scale = minOf(preferred, widthScale, heightScale).coerceAtLeast(0.05f)
        font.data.setScale(scale)
    }

    fun height(font: BitmapFont, text: String): Float {
        layout.setText(font, text)
        return layout.height
    }

    fun centered(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width / 2f, y)
    }

    /** Centers a label on both axes using the font's measured glyph bounds. */
    fun centeredVertically(font: BitmapFont, text: String, x: Float, centerY: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        val baseline = centerY + layout.height / 2f
        font.draw(batch, text, x - layout.width / 2f, baseline)
    }

    fun right(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width, y)
    }

    fun shadowed(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        font.color = UiTheme.SHADOW
        font.draw(batch, text, x + 2f, y - 2f)
        font.color = color
        font.draw(batch, text, x, y)
    }

    fun reset(vararg fonts: BitmapFont) {
        fonts.forEach { it.data.setScale(1f) }
    }
}