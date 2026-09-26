package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

/** Centralized UI font ownership and the documented Orbitron/Kenney Future split. */
class UiFontSet private constructor(
    val title: BitmapFont,
    val body: BitmapFont,
    val caption: BitmapFont
) {
    fun dispose() {
        title.dispose()
        body.dispose()
        caption.dispose()
    }

    companion object {
        private val characters = FreeTypeFontGenerator.DEFAULT_CHARS + "0123456789:-/+%•"

        fun load(): UiFontSet = UiFontSet(
            title = generate("fonts/Orbitron-Medium.ttf", 64, 0.25f, 0.32f),
            body = generate("fonts/Kenney-Future.ttf", 34, 0.14f, 0.20f),
            caption = generate("fonts/Kenney-Future.ttf", 24, 0.10f, 0.18f)
        )

        private fun generate(path: String, size: Int, border: Float, shadowAlpha: Float): BitmapFont {
            val generator = FreeTypeFontGenerator(Gdx.files.internal(path))
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size = size
                color = Color.WHITE
                borderWidth = border
                borderColor = Color(0f, 0f, 0f, shadowAlpha)
                shadowOffsetX = 1
                shadowOffsetY = 1
                shadowColor = Color(0f, 0f, 0f, shadowAlpha)
                characters = Companion.characters
                kerning = true
                genMipMaps = false
                minFilter = Texture.TextureFilter.Linear
                magFilter = Texture.TextureFilter.Linear
            }
            return try {
                generator.generateFont(parameter)
            } finally {
                generator.dispose()
            }
        }
    }
}
