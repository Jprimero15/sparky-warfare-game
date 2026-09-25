package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class SettingsRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {
    fun draw(
        width: Float,
        height: Float,
        safe: SafeArea,
        sfx: Rectangle,
        haptics: Rectangle,
        volume: Rectangle,
        swap: Rectangle,
        menu: Rectangle,
        muted: Boolean,
        hapticsMuted: Boolean,
        volumeValue: Float,
        drawButton: (Rectangle, Color) -> Unit,
        drawCentered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val cx = (safe.left + safe.right) / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.9f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.012f, 0.035f, 0.05f, 0.98f)
        shape.rect(width * 0.14f, height * 0.08f, width * 0.72f, height * 0.84f)
        shape.color = Color(0.18f, 0.78f, 1f, 0.86f)
        shape.rect(width * 0.14f, height * 0.92f - 4f, width * 0.72f, 4f)
        shape.color = Color(1f, 0.25f, 0.34f, 0.68f)
        shape.rect(width * 0.86f - 3f, height * 0.92f - 32f, 3f, 28f)

        drawButton(sfx, if (muted) Color(0.18f, 0.07f, 0.08f, 0.95f) else Color(0.03f, 0.17f, 0.2f, 0.95f))
        drawButton(haptics, if (hapticsMuted) Color(0.18f, 0.07f, 0.08f, 0.95f) else Color(0.03f, 0.17f, 0.2f, 0.95f))
        shape.color = Color(0.015f, 0.055f, 0.075f, 1f)
        shape.rect(volume.x, volume.y, volume.width, volume.height)
        shape.color = Color(0.25f, 0.85f, 1f, 0.82f)
        shape.rect(volume.x, volume.y, volume.width * volumeValue.coerceIn(0f, 1f), volume.height)
        drawButton(swap, Color(0.03f, 0.17f, 0.2f, 0.95f))
        drawButton(menu, Color(0.03f, 0.08f, 0.11f, 0.98f))
        shape.end()

        batch.begin()
        fit("SETTINGS", width * 0.50f, 0.96f, 0.74f)
        drawCentered("SETTINGS", cx, height * 0.81f, Color(0.62f, 0.94f, 1f, 1f))

        fit("SYSTEM / AUDIO / CONTROLS", width * 0.58f, 0.48f, 0.36f)
        drawCentered("SYSTEM / AUDIO / CONTROLS", cx, height * 0.735f, Color(0.4f, 0.58f, 0.63f, 1f))

        fit("SFX: " + if (muted) "MUTED" else "ON", sfx.width - 24f, 0.62f, 0.48f)
        drawCentered("SFX: " + if (muted) "MUTED" else "ON", sfx.x + sfx.width / 2f, sfx.y + sfx.height / 2f + 8f, Color.WHITE)
        fit("HAPTICS: " + if (hapticsMuted) "MUTED" else "ON", haptics.width - 24f, 0.62f, 0.48f)
        drawCentered("HAPTICS: " + if (hapticsMuted) "MUTED" else "ON", haptics.x + haptics.width / 2f, haptics.y + haptics.height / 2f + 8f, Color.WHITE)

        fit("MASTER VOLUME  100%", volume.width - 18f, 0.46f, 0.34f)
        drawCentered("MASTER VOLUME  " + (volumeValue * 100f).toInt() + "%", volume.x + volume.width / 2f, volume.y + volume.height + 18f, Color(0.5f, 0.72f, 0.77f, 1f))

        fit("SWAP MOVE / FIRE", swap.width - 24f, 0.58f, 0.46f)
        drawCentered("SWAP MOVE / FIRE", swap.x + swap.width / 2f, swap.y + swap.height / 2f + 8f, Color(0.84f, 0.95f, 0.97f, 1f))
        fit("MAIN MENU", menu.width - 24f, 0.58f, 0.46f)
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.72f, 0.86f, 0.9f, 1f))

        font.data.setScale(1f)
        batch.end()
    }
}
