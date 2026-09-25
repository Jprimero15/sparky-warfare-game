package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class SettingsRenderer(
    private val shape:ShapeRenderer,
    private val batch:SpriteBatch,
    private val titleFont:BitmapFont,
    private val bodyFont:BitmapFont,
    private val text:UiText
){
    fun draw(width:Float,height:Float,safe:SafeArea,sfx:Rectangle,haptics:Rectangle,volume:Rectangle,swap:Rectangle,menu:Rectangle,muted:Boolean,hapticsMuted:Boolean,volumeValue:Float,drawButton:(Rectangle,Color)->Unit){
        val cx=(safe.left+safe.right)/2f;val cy=(safe.bottom+safe.top)/2f
        val sw=safe.right-safe.left;val sh=safe.top-safe.bottom
        val panelW=minOf(sw*0.82f,1020f).coerceAtLeast(280f);val panelH=minOf(sh*0.88f,600f).coerceAtLeast(250f)
        val x=cx-panelW/2f;val y=cy-panelH/2f
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=UiTheme.BLACK;shape.color.a=0.84f;shape.rect(0f,0f,width,height)
        UiShapes.roundedRect(shape,x,y,panelW,panelH,28f,UiTheme.PANEL)
        UiShapes.roundedRect(shape,x+6f,y+6f,panelW-12f,panelH-12f,24f,UiTheme.INNER)
        drawButton(sfx,if(muted)UiTheme.DANGER else UiTheme.CYAN)
        drawButton(haptics,if(hapticsMuted)UiTheme.DANGER else UiTheme.CYAN)
        UiShapes.roundedRect(shape,volume.x,volume.y,volume.width,volume.height,14f,UiTheme.PANEL_DARK)
        UiShapes.roundedRect(shape,volume.x,volume.y,volume.width*volumeValue.coerceIn(0f,1f),volume.height,14f,UiTheme.CYAN)
        drawButton(swap,UiTheme.PANEL_DARK);drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"SYSTEM CONFIG",panelW-90f,1.2f,0.8f);text.centered(titleFont,"SYSTEM CONFIG",cx,y+panelH-56f,UiTheme.CYAN)
                command(sfx,"SFX: "+if(muted)"OFF" else "ON",if(muted)UiTheme.DANGER else UiTheme.WHITE)
        command(haptics,"HAPTICS: "+if(hapticsMuted)"OFF" else "ON",if(hapticsMuted)UiTheme.DANGER else UiTheme.WHITE)
        text.fit(bodyFont,"VOLUME "+(volumeValue*100f).toInt()+"%",volume.width-20f,0.72f,0.5f);text.centered(bodyFont,"VOLUME "+(volumeValue*100f).toInt()+"%",volume.x+volume.width/2f,volume.y+volume.height+22f,UiTheme.DIM)
        command(swap,"SWAP: LEFT / RIGHT",UiTheme.WHITE)
        command(menu,"BACK",UiTheme.WHITE)
        text.reset(titleFont,bodyFont);batch.end()
    }
    private fun command(r:Rectangle,t:String,tc:Color){
        val cx=r.x+r.width/2f
        text.fitWithin(titleFont,t,r.width-46f,r.height*0.50f,0.72f,0.50f)
        val titleHeight=text.height(titleFont,t)
        text.centered(titleFont,t,cx,r.y+r.height/2f+titleHeight/2f,tc)
    }
}
