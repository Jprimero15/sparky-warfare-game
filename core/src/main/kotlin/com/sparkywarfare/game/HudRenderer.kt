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
    fun safeArea(width: Float, height: Float): SafeArea = SafeArea(
        Gdx.graphics.safeInsetLeft.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
        (width - Gdx.graphics.safeInsetRight).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN),
        Gdx.graphics.safeInsetBottom.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
        (height - Gdx.graphics.safeInsetTop).coerceAtMost(height - GameConfig.Ui.SAFE_MARGIN)
    )

    fun fit(text: String, maxWidth: Float, preferred: Float, minimum: Float) {
        font.data.setScale(1f)
        layout.setText(font, text)
        val scale = if (layout.width > 0f) (maxWidth / layout.width).coerceAtMost(preferred) else preferred
        font.data.setScale(scale.coerceAtLeast(minimum))
    }

    fun centered(text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width / 2f, y)
    }

    fun right(text: String, x: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, x - layout.width, y)
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

