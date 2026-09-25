package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class GameOverRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
) {
    fun draw(width:Float,height:Float,safe:SafeArea,retry:Rectangle,menu:Rectangle,score:Int,best:Int,wave:Int,kills:Int,combo:Int,drawButton:(Rectangle,Color)->Unit){
        val cx=(safe.left+safe.right)/2f; val cy=(safe.bottom+safe.top)/2f
        val sw=safe.right-safe.left; val sh=safe.top-safe.bottom
        val panelW=minOf(sw*0.78f,980f).coerceAtLeast(320f); val panelH=minOf(sh*0.78f,520f).coerceAtLeast(260f)
        val x=cx-panelW/2f; val y=cy-panelH/2f
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=UiTheme.BLACK; shape.color.a=0.88f; shape.rect(0f,0f,width,height)
        UiShapes.roundedRect(shape,x,y,panelW,panelH,28f,UiTheme.PANEL)
        UiShapes.roundedRect(shape,x+6f,y+6f,panelW-12f,panelH-12f,24f,UiTheme.INNER)
        drawButton(retry,UiTheme.PANEL_DARK); drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"SYSTEM FAILURE",panelW-80f,1.25f,0.82f); text.centered(titleFont,"SYSTEM FAILURE",cx,y+panelH-56f,UiTheme.DANGER)
        text.fit(bodyFont,"SCORE   $score",panelW*0.34f,0.86f,0.58f); text.centered(bodyFont,"SCORE   $score",x+panelW*0.30f,y+panelH-142f,UiTheme.WHITE)
        text.fit(bodyFont,"BEST   $best",panelW*0.30f,0.86f,0.58f); text.centered(bodyFont,"BEST   $best",x+panelW*0.70f,y+panelH-142f,UiTheme.WHITE)
        text.fit(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",panelW-90f,0.7f,0.48f)
        text.centered(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",cx,y+panelH-177f,UiTheme.DIM)
        command(retry,"RETRY",UiTheme.WHITE)
        command(menu,"MENU",UiTheme.WHITE)
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
