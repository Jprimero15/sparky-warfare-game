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
        shape.color = UiTheme.BLACK; shape.color.a = 0.8f; shape.rect(0f,0f,width,height)
        shape.color = UiTheme.PANEL; shape.rect(x,y,panelW,panelH)
        shape.color = UiTheme.INNER; shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color = UiTheme.CYAN; shape.rect(x,y+panelH-4f,panelW,4f)
        shape.color = UiTheme.MAGENTA; shape.rect(x+panelW-4f,y+panelH-38f,4f,34f)
        shape.color = UiTheme.CYAN_SOFT; shape.rect(x+28f,y+panelH-118f,panelW-56f,2f)
        drawButton(resume,UiTheme.CYAN)
        drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"PAUSED",panelW-80f,1.25f,0.82f)
        text.centered(titleFont,"PAUSED",cx,y+panelH-58f,UiTheme.CYAN)
        text.fit(bodyFont,"COMBAT SUSPENDED // SYSTEM HOLD",panelW-100f,0.82f,0.56f)
        text.centered(bodyFont,"COMBAT SUSPENDED // SYSTEM HOLD",cx,y+panelH-94f,UiTheme.MAGENTA)
        command(resume,"RESUME","RETURN TO BATTLE",UiTheme.WHITE,UiTheme.CYAN)
        command(menu,"MAIN MENU","RETURN TO CONSOLE",UiTheme.WHITE,UiTheme.DIM_DARK)
        text.reset(titleFont,bodyFont)
        batch.end()
    }

    private fun command(r: Rectangle,title:String,subtitle:String,titleColor:com.badlogic.gdx.graphics.Color,subtitleColor:com.badlogic.gdx.graphics.Color){
        val cx=r.x+r.width/2f
        text.fit(titleFont,title,r.width-44f,0.78f,0.55f)
        text.centered(titleFont,title,cx,r.y+r.height*0.66f,titleColor)
        text.fit(bodyFont,subtitle,r.width-44f,0.64f,0.44f)
        text.centered(bodyFont,subtitle,cx,r.y+r.height*0.27f,subtitleColor)
    }
}
