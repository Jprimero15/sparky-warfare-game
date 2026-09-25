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
        shape.color=UiTheme.PANEL; shape.rect(x,y,panelW,panelH)
        shape.color=UiTheme.INNER; shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color=UiTheme.DANGER; shape.rect(x,y+panelH-4f,panelW,4f)
        shape.color=UiTheme.CYAN; shape.rect(x,y+4f,panelW*0.34f,3f)
        shape.color=UiTheme.DANGER; shape.color.a=0.18f; shape.rect(x+30f,y+panelH-128f,panelW-60f,2f)
        drawButton(retry,UiTheme.PANEL_DARK); drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"SYSTEM FAILURE",panelW-80f,1.25f,0.82f); text.centered(titleFont,"SYSTEM FAILURE",cx,y+panelH-56f,UiTheme.DANGER)
        text.fit(bodyFont,"COMBAT SESSION TERMINATED",panelW-100f,0.82f,0.56f); text.centered(bodyFont,"COMBAT SESSION TERMINATED",cx,y+panelH-91f,UiTheme.DIM)
        text.fit(bodyFont,"SCORE   $score",panelW*0.34f,0.86f,0.58f); text.centered(bodyFont,"SCORE   $score",x+panelW*0.30f,y+panelH-142f,UiTheme.WHITE)
        text.fit(bodyFont,"BEST   $best",panelW*0.30f,0.86f,0.58f); text.centered(bodyFont,"BEST   $best",x+panelW*0.70f,y+panelH-142f,UiTheme.WHITE)
        text.fit(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",panelW-90f,0.7f,0.48f)
        text.centered(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",cx,y+panelH-177f,UiTheme.DIM)
        command(retry,"REDEPLOY","START ANOTHER RUN",UiTheme.WHITE,UiTheme.CYAN)
        command(menu,"MAIN MENU","RETURN TO CONSOLE",UiTheme.WHITE,UiTheme.DIM)
        text.reset(titleFont,bodyFont)
        batch.end()
    }

    private fun command(r:Rectangle,t:String,s:String,tc:Color,sc:Color){
        val cx=r.x+r.width/2f
        text.fit(titleFont,t,r.width-44f,0.74f,0.54f); text.centered(titleFont,t,cx,r.y+r.height*0.66f,tc)
        text.fit(bodyFont,s,r.width-44f,0.62f,0.42f); text.centered(bodyFont,s,cx,r.y+r.height*0.25f,sc)
    }
}
