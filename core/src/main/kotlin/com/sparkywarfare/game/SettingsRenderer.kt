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
        shape.color=UiTheme.PANEL;shape.rect(x,y,panelW,panelH);shape.color=UiTheme.INNER;shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color=UiTheme.CYAN;shape.rect(x,y+panelH-4f,panelW,4f);shape.color=UiTheme.MAGENTA;shape.rect(x+panelW-4f,y+panelH-38f,4f,34f)
        shape.color=UiTheme.CYAN_SOFT;shape.rect(x+30f,y+panelH-116f,panelW-60f,2f)
        drawButton(sfx,if(muted)UiTheme.DANGER else UiTheme.CYAN)
        drawButton(haptics,if(hapticsMuted)UiTheme.DANGER else UiTheme.CYAN)
        shape.color=UiTheme.PANEL_DARK;shape.rect(volume.x,volume.y,volume.width,volume.height)
        shape.color=UiTheme.CYAN;shape.rect(volume.x,volume.y,volume.width*volumeValue.coerceIn(0f,1f),volume.height)
        shape.color=UiTheme.CYAN_SOFT;shape.rect(volume.x,volume.y+volume.height-4f,volume.width,4f)
        drawButton(swap,UiTheme.PANEL_DARK);drawButton(menu,UiTheme.PANEL_DARK)
        shape.end()
        batch.begin()
        text.fit(titleFont,"SYSTEM CONFIG",panelW-90f,1.2f,0.8f);text.centered(titleFont,"SYSTEM CONFIG",cx,y+panelH-56f,UiTheme.CYAN)
        text.fit(bodyFont,"DEVICE CONTROL MATRIX",panelW-110f,0.82f,0.56f);text.centered(bodyFont,"DEVICE CONTROL MATRIX",cx,y+panelH-91f,UiTheme.MAGENTA)
        command(sfx,"SFX: "+if(muted)"MUTED" else "ONLINE","TOGGLE SOUND EFFECTS",if(muted)UiTheme.DANGER else UiTheme.WHITE,UiTheme.DIM)
        command(haptics,"HAPTICS: "+if(hapticsMuted)"MUTED" else "ONLINE","TOGGLE DEVICE FEEDBACK",if(hapticsMuted)UiTheme.DANGER else UiTheme.WHITE,UiTheme.DIM)
        text.fit(bodyFont,"MASTER VOLUME  "+(volumeValue*100f).toInt()+"%",volume.width-20f,0.72f,0.5f);text.centered(bodyFont,"MASTER VOLUME  "+(volumeValue*100f).toInt()+"%",volume.x+volume.width/2f,volume.y+volume.height+22f,UiTheme.DIM)
        command(swap,"CONTROL SIDES","SWAP MOVE / FIRE",UiTheme.WHITE,UiTheme.CYAN)
        command(menu,"BACK","RETURN TO CONSOLE",UiTheme.WHITE,UiTheme.DIM)
        text.reset(titleFont,bodyFont);batch.end()
    }
    private fun command(r:Rectangle,t:String,s:String,tc:Color,sc:Color){
        val cx=r.x+r.width/2f
        text.fitWithin(titleFont,t,r.width-46f,r.height*0.40f,0.66f,0.48f)
        val titleHeight=text.height(titleFont,t)
        text.fitWithin(bodyFont,s,r.width-46f,r.height*0.28f,0.6f,0.42f)
        val subtitleHeight=text.height(bodyFont,s)
        val centerY=r.y+r.height/2f
        val gap=(r.height*0.10f).coerceIn(4f,8f)
        text.centered(titleFont,t,cx,centerY+gap/2f+titleHeight/2f,tc)
        text.centered(bodyFont,s,cx,centerY-gap/2f-subtitleHeight/2f,sc)
    }
}
