package com.sparkywarfare.game

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class PauseRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
) {
    fun draw(width: Float, height: Float, safe: SafeArea, resume: Rectangle, menu: Rectangle, drawButton: (Rectangle, com.badlogic.gdx.graphics.Color) -> Unit) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = safe.right - safe.left
        val sh = safe.top - safe.bottom
        val panelW = minOf(sw * 0.72f, 900f).coerceAtLeast(300f)
        val panelH = minOf(sh * 0.72f, 500f).coerceAtLeast(240f)
        val x = cx - panelW / 2f
        val y = cy - panelH / 2f
        shape.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.overlay(shape, width, height)
        UiShapes.panel(shape, x, y, panelW, panelH)
        drawButton(resume,UiTheme.ACCENT)
        drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"PAUSED",panelW-80f,1.25f,0.82f)
        text.centered(titleFont,"PAUSED",cx,y+panelH-58f,UiTheme.ACCENT)
        command(resume,"RESUME",UiTheme.TEXT_PRIMARY)
        command(menu,"MENU",UiTheme.TEXT_PRIMARY)
        text.reset(titleFont,bodyFont)
        batch.end()
    }

    private fun command(r: Rectangle, title: String, color: com.badlogic.gdx.graphics.Color) {
        val cx = r.x + r.width / 2f
        text.fitWithin(titleFont, title, r.width - 44f, r.height * 0.50f, 0.84f, 0.58f)
        val titleHeight = text.height(titleFont, title)
        text.centered(titleFont, title, cx, r.y + r.height / 2f + titleHeight / 2f, color)
    }
}
