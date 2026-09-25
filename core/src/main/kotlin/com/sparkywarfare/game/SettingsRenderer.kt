package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class SettingsRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
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
        drawButton: (Rectangle, Color) -> Unit
    ) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = safe.right - safe.left
        val sh = safe.top - safe.bottom
        val panelW = minOf(sw * 0.82f, 1000f).coerceAtLeast(360f)
        val panelH = minOf(sh * 0.86f, 600f).coerceAtLeast(300f)
        val x = cx - panelW / 2f
        val y = cy - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.overlay(shape, width, height)
        UiShapes.glassPanel(shape, x, y, panelW, panelH)

        drawButton(sfx, if (muted) UiTheme.DANGER else UiTheme.CYAN)
        drawButton(haptics, if (hapticsMuted) UiTheme.DANGER else UiTheme.CYAN)

        UiShapes.glassCard(shape, volume.x, volume.y - 4f, volume.width, volume.height + 8f, 16f, UiTheme.CYAN)
        val fill = volume.width * volumeValue.coerceIn(0f, 1f)
        if (fill > 0f) {
            UiShapes.gradientRoundedRect(
                shape,
                volume.x,
                volume.y,
                fill,
                volume.height,
                15f,
                UiTheme.CYAN,
                UiTheme.MAGENTA,
                8
            )
        }
        shape.color = UiTheme.WHITE
        shape.circle(volume.x + fill, volume.y + volume.height / 2f, 10f, 28)

        drawButton(swap, UiTheme.PANEL_DARK)
        drawButton(menu, UiTheme.PANEL_DARK)
        shape.end()

        batch.begin()
        text.fitWithin(titleFont, "SETTINGS", panelW - 90f, 54f, 1.15f, 0.72f)
        text.centered(titleFont, "SETTINGS", cx, y + panelH - 58f, UiTheme.TEXT_PRIMARY)

        val subtitleY = y + panelH - 94f
        text.fitWithin(bodyFont, "MAKE IT YOURS", panelW - 100f, 24f, 0.62f, 0.46f)
        text.centered(bodyFont, "MAKE IT YOURS", cx, subtitleY, UiTheme.CYAN)

        command(sfx, "SFX  " + if (muted) "OFF" else "ON", if (muted) UiTheme.DANGER else UiTheme.TEXT_PRIMARY)
        command(haptics, "HAPTICS  " + if (hapticsMuted) "OFF" else "ON", if (hapticsMuted) UiTheme.DANGER else UiTheme.TEXT_PRIMARY)

        val volumeLabel = "VOLUME  " + (volumeValue * 100f).toInt() + "%"
        text.fitWithin(bodyFont, volumeLabel, volume.width - 24f, 28f, 0.64f, 0.46f)
        text.centered(bodyFont, volumeLabel, volume.x + volume.width / 2f, volume.y + volume.height + 28f, UiTheme.TEXT_SECONDARY)

        command(swap, "SWAP  MOVE / FIRE", UiTheme.TEXT_PRIMARY)
        command(menu, "BACK", UiTheme.TEXT_PRIMARY)

        text.reset(titleFont, bodyFont)
        batch.end()
    }

    private fun command(rect: Rectangle, value: String, color: Color) {
        val cx = rect.x + rect.width / 2f
        text.fitWithin(
            titleFont,
            value,
            rect.width - 52f,
            rect.height * 0.44f,
            UiTheme.Metrics.BUTTON_TEXT_SCALE,
            UiTheme.Metrics.BUTTON_TEXT_MIN_SCALE
        )
        val h = text.height(titleFont, value)
        text.centered(titleFont, value, cx, rect.y + rect.height / 2f + h / 2f, color)
    }
}
