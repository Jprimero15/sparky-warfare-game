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
        fit: (String, Float, Float, Float) -> Unit
    ) {
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.9f)
        shape.rect(0f, 0f, width, height)
        shape.color = Color(0.015f, 0.035f, 0.05f, 0.96f)
        shape.rect(width * 0.14f, height * 0.08f, width * 0.72f, height * 0.84f)
        shape.color = Color(0.25f, 0.85f, 1f, 0.8f)
        shape.rect(width * 0.14f, height * 0.92f - 3f, width * 0.72f, 3f)
        drawButton(sfx, if (muted) Color(0.18f, 0.07f, 0.08f, 0.95f) else Color(0.03f, 0.16f, 0.19f, 0.95f))
        drawButton(haptics, if (hapticsMuted) Color(0.18f, 0.07f, 0.08f, 0.95f) else Color(0.03f, 0.16f, 0.19f, 0.95f))
        shape.color = Color(0.015f, 0.055f, 0.075f, 1f)
        shape.rect(volume.x, volume.y, volume.width, volume.height)
        shape.color = Color(0.25f, 0.85f, 1f, 0.82f)
        shape.rect(volume.x, volume.y, volume.width * volumeValue.coerceIn(0f, 1f), volume.height)
        drawButton(swap, Color(0.03f, 0.16f, 0.19f, 0.95f))
        drawButton(menu, Color(0.03f, 0.08f, 0.11f, 0.98f))
        shape.end()

        batch.begin()
        fit("SETTINGS", width * 0.58f, 2f, 1.1f)
        drawCentered("SETTINGS", (safe.left + safe.right) / 2f, height * 0.78f, Color(0.58f, 0.93f, 1f, 1f))
        font.data.setScale(0.58f)
        drawCentered("SYSTEM / AUDIO / CONTROLS", (safe.left + safe.right) / 2f, height * 0.72f, Color(0.38f, 0.56f, 0.62f, 1f))
        font.data.setScale(0.7f)
        drawCentered("SFX: " + if (muted) "MUTED" else "ON", sfx.x + sfx.width / 2f, sfx.y + sfx.height / 2f + 8f, Color.WHITE)
        drawCentered("HAPTICS: " + if (hapticsMuted) "MUTED" else "ON", haptics.x + haptics.width / 2f, haptics.y + haptics.height / 2f + 8f, Color.WHITE)
        font.data.setScale(0.52f)
        drawCentered("MASTER VOLUME  " + (volumeValue * 100f).toInt() + "%", volume.x + volume.width / 2f, volume.y + volume.height + 18f, Color(0.5f, 0.72f, 0.77f, 1f))
        font.data.setScale(0.66f)
        drawCentered("SWAP MOVE / FIRE", swap.x + swap.width / 2f, swap.y + swap.height / 2f + 8f, Color(0.84f, 0.95f, 0.97f, 1f))
        drawCentered("MAIN MENU", menu.x + menu.width / 2f, menu.y + menu.height / 2f + 8f, Color(0.72f, 0.86f, 0.9f, 1f))
        batch.end()
    }
}
