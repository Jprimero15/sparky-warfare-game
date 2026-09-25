package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/** Shared text fitting/drawing primitives used by every UI renderer. */
class UiText(private val batch: SpriteBatch) {
    private val layout = GlyphLayout()

    fun fit(font: BitmapFont, text: String, maxWidth: Float, preferred: Float, minimum: Float) {
        font.data.setScale(1f)
        layout.setText(font, text)
        if (layout.width <= 0f) {
            font.data.setScale(preferred.coerceAtLeast(0.05f))
            return
        }
        val widthScale = (maxWidth / layout.width).coerceAtLeast(0.05f)
        // Available width always wins so text cannot escape a panel at extreme ratios.
        val scale = if (widthScale < minimum) widthScale else minOf(preferred, widthScale)
        font.data.setScale(scale.coerceAtLeast(0.05f))
    }

    fun centered(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width / 2f, y)
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