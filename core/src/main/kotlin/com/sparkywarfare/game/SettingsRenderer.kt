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
    private val panel = Color(0.006f, 0.016f, 0.03f, 0.98f)
    private val inner = Color(0.016f, 0.038f, 0.06f, 0.97f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val dim = Color(0.4f, 0.59f, 0.67f, 1f)
    private val danger = Color(1f, 0.28f, 0.38f, 1f)

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
        val panelW = (safe.right - safe.left).coerceIn(380f, 900f) * 0.88f
        val panelH = (safe.top - safe.bottom).coerceAtLeast(430f) * 0.9f
        val panelX = cx - panelW / 2f
        val panelY = (safe.bottom + safe.top) / 2f - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.86f)
        shape.rect(0f, 0f, width, height)
        shape.color = panel
        shape.rect(panelX, panelY, panelW, panelH)
        shape.color = inner
        shape.rect(panelX + 6f, panelY + 6f, panelW - 12f, panelH - 12f)
        shape.color = cyan
        shape.rect(panelX, panelY + panelH - 3f, panelW, 3f)
        shape.color = magenta
        shape.rect(panelX + panelW - 4f, panelY + panelH - 34f, 4f, 31f)
        shape.color = Color(0.18f, 0.9f, 1f, 0.16f)
        shape.rect(panelX + 28f, panelY + panelH - 118f, panelW - 56f, 2f)

        drawButton(sfx, if (muted) Color(0.18f, 0.045f, 0.07f, 0.98f) else Color(0.02f, 0.22f, 0.28f, 0.98f))
        drawButton(haptics, if (hapticsMuted) Color(0.18f, 0.045f, 0.07f, 0.98f) else Color(0.02f, 0.22f, 0.28f, 0.98f))

        shape.color = Color(0.01f, 0.025f, 0.04f, 1f)
        shape.rect(volume.x, volume.y, volume.width, volume.height)
        shape.color = cyan
        shape.rect(volume.x, volume.y, volume.width * volumeValue.coerceIn(0f, 1f), volume.height)
        shape.color = Color(0.65f, 0.92f, 1f, 0.22f)
        shape.rect(volume.x, volume.y + volume.height - 4f, volume.width, 4f)

        drawButton(swap, Color(0.05f, 0.07f, 0.14f, 0.98f))
        drawButton(menu, Color(0.035f, 0.055f, 0.09f, 0.98f))
        shape.end()

        batch.begin()
        fit("SYSTEM CONFIG", panelW - 70f, 1.2f, 0.78f)
        drawCentered("SYSTEM CONFIG", cx, panelY + panelH - 58f, cyan)
        fit("AUDIO // HAPTICS // CONTROL MATRIX", panelW - 96f, 0.46f, 0.34f)
        drawCentered("AUDIO // HAPTICS // CONTROL MATRIX", cx, panelY + panelH - 92f, magenta)

        drawCommand(sfx, "SFX: " + if (muted) "MUTED" else "ONLINE", "TOGGLE SOUND EFFECTS",
            if (muted) danger else Color.WHITE, dim, drawCentered, fit)
        drawCommand(haptics, "HAPTICS: " + if (hapticsMuted) "MUTED" else "ONLINE", "TOGGLE DEVICE FEEDBACK",
            if (hapticsMuted) danger else Color.WHITE, dim, drawCentered, fit)

        fit("MASTER VOLUME   " + (volumeValue * 100f).toInt() + "%", volume.width - 18f, 0.46f, 0.34f)
        drawCentered(
            "MASTER VOLUME   " + (volumeValue * 100f).toInt() + "%",
            volume.x + volume.width / 2f,
            volume.y + volume.height + 18f,
            dim
        )

        drawCommand(swap, "CONTROL MATRIX", "SWAP MOVE / FIRE",
            Color(0.88f, 0.94f, 1f, 1f), cyan, drawCentered, fit)
        drawCommand(menu, "BACK TO CONSOLE", "SAVE AND RETURN",
            Color(0.86f, 0.9f, 0.96f, 1f), dim, drawCentered, fit)
        font.data.setScale(1f)
        batch.end()
    }

    private fun drawCommand(
        rect: Rectangle,
        title: String,
        subtitle: String,
        titleColor: Color,
        subtitleColor: Color,
        centered: (String, Float, Float, Color) -> Unit,
        fit: (String, Float, Float, Float) -> Float
    ) {
        val cx = rect.x + rect.width / 2f
        fit(title, rect.width - 46f, 0.64f, 0.48f)
        centered(title, cx, rect.y + rect.height * 0.62f, titleColor)
        fit(subtitle, rect.width - 46f, 0.34f, 0.26f)
        centered(subtitle, cx, rect.y + rect.height * 0.22f, subtitleColor)
    }
}
