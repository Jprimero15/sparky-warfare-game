package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class SettingsRenderer(
    private val shape:ShapeRenderer,
    private val batch:SpriteBatch,
    private val titleFont:BitmapFont,
    private val bodyFont:BitmapFont
){
    private val panel=Color(0.006f,0.016f,0.03f,0.98f)
    private val inner=Color(0.016f,0.038f,0.06f,0.97f)
    private val cyan=Color(0.18f,0.9f,1f,1f)
    private val magenta=Color(0.78f,0.24f,1f,1f)
    private val dim=Color(0.43f,0.62f,0.69f,1f)
    private val danger=Color(1f,0.28f,0.38f,1f)
    private val layout=GlyphLayout()

    fun draw(width:Float,height:Float,safe:SafeArea,sfx:Rectangle,haptics:Rectangle,volume:Rectangle,swap:Rectangle,menu:Rectangle,muted:Boolean,hapticsMuted:Boolean,volumeValue:Float,drawButton:(Rectangle,Color)->Unit){
        val cx=(safe.left+safe.right)/2f;val cy=(safe.bottom+safe.top)/2f
        val sw=safe.right-safe.left;val sh=safe.top-safe.bottom
        val panelW=minOf(sw*0.82f,1020f).coerceAtLeast(280f);val panelH=minOf(sh*0.88f,600f).coerceAtLeast(250f)
        val x=cx-panelW/2f;val y=cy-panelH/2f
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=Color(0f,0f,0f,0.84f);shape.rect(0f,0f,width,height)
        shape.color=panel;shape.rect(x,y,panelW,panelH);shape.color=inner;shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color=cyan;shape.rect(x,y+panelH-4f,panelW,4f);shape.color=magenta;shape.rect(x+panelW-4f,y+panelH-38f,4f,34f)
        shape.color=Color(0.18f,0.9f,1f,0.14f);shape.rect(x+30f,y+panelH-116f,panelW-60f,2f)
        drawButton(sfx,if(muted)Color(0.18f,0.045f,0.07f,0.98f)else Color(0.02f,0.22f,0.28f,0.98f))
        drawButton(haptics,if(hapticsMuted)Color(0.18f,0.045f,0.07f,0.98f)else Color(0.02f,0.22f,0.28f,0.98f))
        shape.color=Color(0.01f,0.025f,0.04f,1f);shape.rect(volume.x,volume.y,volume.width,volume.height)
        shape.color=cyan;shape.rect(volume.x,volume.y,volume.width*volumeValue.coerceIn(0f,1f),volume.height)
        shape.color=Color(0.65f,0.92f,1f,0.22f);shape.rect(volume.x,volume.y+volume.height-4f,volume.width,4f)
        drawButton(swap,Color(0.05f,0.07f,0.14f,0.98f));drawButton(menu,Color(0.035f,0.055f,0.09f,0.98f))
        shape.end()

        batch.begin()
        fit(titleFont,"SYSTEM CONFIG",panelW-90f,1.2f,0.8f);centered(titleFont,"SYSTEM CONFIG",cx,y+panelH-56f,cyan)
        fit(bodyFont,"DEVICE CONTROL MATRIX",panelW-110f,0.82f,0.56f);centered(bodyFont,"DEVICE CONTROL MATRIX",cx,y+panelH-91f,magenta)
        command(sfx,"SFX: "+if(muted)"MUTED" else "ONLINE","TOGGLE SOUND EFFECTS",if(muted)danger else Color.WHITE,dim)
        command(haptics,"HAPTICS: "+if(hapticsMuted)"MUTED" else "ONLINE","TOGGLE DEVICE FEEDBACK",if(hapticsMuted)danger else Color.WHITE,dim)
        fit(bodyFont,"MASTER VOLUME  "+(volumeValue*100f).toInt()+"%",volume.width-20f,0.72f,0.5f);centered(bodyFont,"MASTER VOLUME  "+(volumeValue*100f).toInt()+"%",volume.x+volume.width/2f,volume.y+volume.height+22f,dim)
        command(swap,"CONTROL SIDES","SWAP MOVE / FIRE",Color.WHITE,cyan)
        command(menu,"BACK","RETURN TO CONSOLE",Color(0.86f,0.9f,0.96f,1f),dim)
        reset();batch.end()
    }
    private fun command(r:Rectangle,t:String,s:String,tc:Color,sc:Color){val cx=r.x+r.width/2f;fit(titleFont,t,r.width-46f,0.66f,0.48f);centered(titleFont,t,cx,r.y+r.height*0.66f,tc);fit(bodyFont,s,r.width-46f,0.6f,0.42f);centered(bodyFont,s,cx,r.y+r.height*0.25f,sc)}
    private fun fit(f:BitmapFont,t:String,max:Float,pref:Float,min:Float){f.data.setScale(1f);layout.setText(f,t);val ws=if(layout.width>0)max/layout.width else pref;f.data.setScale(minOf(pref,ws.coerceAtLeast(min),ws))}
    private fun centered(f:BitmapFont,t:String,x:Float,y:Float,c:Color){layout.setText(f,t);f.color=c;f.draw(batch,t,x-layout.width/2f,y)}
    private fun reset(){titleFont.data.setScale(1f);bodyFont.data.setScale(1f)}
}
