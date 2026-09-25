package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class GameOverRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont
) {
    private val panel=Color(0.028f,0.008f,0.018f,0.98f)
    private val inner=Color(0.055f,0.018f,0.035f,0.96f)
    private val danger=Color(1f,0.2f,0.34f,1f)
    private val cyan=Color(0.18f,0.9f,1f,1f)
    private val dim=Color(0.55f,0.67f,0.72f,1f)
    private val layout=GlyphLayout()

    fun draw(width:Float,height:Float,safe:SafeArea,retry:Rectangle,menu:Rectangle,score:Int,best:Int,wave:Int,kills:Int,combo:Int,drawButton:(Rectangle,Color)->Unit){
        val cx=(safe.left+safe.right)/2f; val cy=(safe.bottom+safe.top)/2f
        val sw=safe.right-safe.left; val sh=safe.top-safe.bottom
        val panelW=minOf(sw*0.78f,980f).coerceAtLeast(320f); val panelH=minOf(sh*0.78f,520f).coerceAtLeast(260f)
        val x=cx-panelW/2f; val y=cy-panelH/2f
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=Color(0f,0f,0f,0.88f);shape.rect(0f,0f,width,height)
        shape.color=panel;shape.rect(x,y,panelW,panelH)
        shape.color=inner;shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color=danger;shape.rect(x,y+panelH-4f,panelW,4f)
        shape.color=cyan;shape.rect(x,y+4f,panelW*0.34f,3f)
        shape.color=Color(1f,0.2f,0.34f,0.18f);shape.rect(x+30f,y+panelH-128f,panelW-60f,2f)
        drawButton(retry,Color(0.02f,0.22f,0.34f,0.98f));drawButton(menu,Color(0.06f,0.05f,0.1f,0.98f))
        shape.end()

        batch.begin()
        fit(titleFont,"SYSTEM FAILURE",panelW-80f,1.25f,0.82f);centered(titleFont,"SYSTEM FAILURE",cx,y+panelH-56f,danger)
        fit(bodyFont,"COMBAT SESSION TERMINATED",panelW-100f,0.82f,0.56f);centered(bodyFont,"COMBAT SESSION TERMINATED",cx,y+panelH-91f,dim)
        fit(bodyFont,"SCORE   $score",panelW*0.34f,0.86f,0.58f);centered(bodyFont,"SCORE   $score",x+panelW*0.30f,y+panelH-142f,Color.WHITE)
        fit(bodyFont,"BEST   $best",panelW*0.30f,0.86f,0.58f);centered(bodyFont,"BEST   $best",x+panelW*0.70f,y+panelH-142f,Color(0.72f,0.86f,0.92f,1f))
        fit(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",panelW-90f,0.7f,0.48f)
        centered(bodyFont,"WAVE $wave   //   KILLS $kills   //   COMBO x$combo",cx,y+panelH-177f,dim)
        command(retry,"REDEPLOY","START ANOTHER RUN",Color.WHITE,cyan)
        command(menu,"MAIN MENU","RETURN TO CONSOLE",Color(0.86f,0.9f,0.96f,1f),dim)
        reset();batch.end()
    }
    private fun command(r:Rectangle,t:String,s:String,tc:Color,sc:Color){val cx=r.x+r.width/2f;fit(titleFont,t,r.width-44f,0.74f,0.54f);centered(titleFont,t,cx,r.y+r.height*0.66f,tc);fit(bodyFont,s,r.width-44f,0.62f,0.42f);centered(bodyFont,s,cx,r.y+r.height*0.25f,sc)}
    private fun fit(f:BitmapFont,t:String,max:Float,pref:Float,min:Float){f.data.setScale(1f);layout.setText(f,t);val ws=if(layout.width>0)max/layout.width else pref;f.data.setScale(minOf(pref,ws.coerceAtLeast(min),ws))}
    private fun centered(f:BitmapFont,t:String,x:Float,y:Float,c:Color){layout.setText(f,t);f.color=c;f.draw(batch,t,x-layout.width/2f,y)}
    private fun reset(){titleFont.data.setScale(1f);bodyFont.data.setScale(1f)}
}
