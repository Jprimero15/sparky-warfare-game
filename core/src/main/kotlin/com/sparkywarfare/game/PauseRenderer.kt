package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class PauseRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont
) {
    private val panel = Color(0.006f, 0.016f, 0.028f, 0.98f)
    private val inner = Color(0.018f, 0.038f, 0.06f, 0.97f)
    private val cyan = Color(0.18f, 0.9f, 1f, 1f)
    private val magenta = Color(0.78f, 0.24f, 1f, 1f)
    private val dim = Color(0.42f, 0.6f, 0.67f, 1f)
    private val layout = GlyphLayout()

    fun draw(width: Float, height: Float, safe: SafeArea, resume: Rectangle, menu: Rectangle, drawButton: (Rectangle, Color) -> Unit) {
        val cx = (safe.left + safe.right) / 2f
        val cy = (safe.bottom + safe.top) / 2f
        val sw = safe.right - safe.left
        val sh = safe.top - safe.bottom
        val panelW = minOf(sw * 0.72f, 900f).coerceAtLeast(300f)
        val panelH = minOf(sh * 0.72f, 500f).coerceAtLeast(240f)
        val x = cx - panelW / 2f
        val y = cy - panelH / 2f

        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f,0f,0f,0.8f); shape.rect(0f,0f,width,height)
        shape.color = panel; shape.rect(x,y,panelW,panelH)
        shape.color = inner; shape.rect(x+6f,y+6f,panelW-12f,panelH-12f)
        shape.color = cyan; shape.rect(x,y+panelH-4f,panelW,4f)
        shape.color = magenta; shape.rect(x+panelW-4f,y+panelH-38f,4f,34f)
        shape.color = Color(0.18f,0.9f,1f,0.16f); shape.rect(x+28f,y+panelH-118f,panelW-56f,2f)
        drawButton(resume, Color(0.02f,0.22f,0.34f,0.98f))
        drawButton(menu, Color(0.045f,0.055f,0.1f,0.98f))
        shape.end()

        batch.begin()
        fit(titleFont,"PAUSED",panelW-80f,1.25f,0.82f)
        centered(titleFont,"PAUSED",cx,y+panelH-58f,cyan)
        fit(bodyFont,"COMBAT SUSPENDED // SYSTEM HOLD",panelW-100f,0.82f,0.56f)
        centered(bodyFont,"COMBAT SUSPENDED // SYSTEM HOLD",cx,y+panelH-94f,magenta)
        command(resume,"RESUME","RETURN TO BATTLE",Color.WHITE,cyan)
        command(menu,"MAIN MENU","RETURN TO CONSOLE",Color(0.86f,0.9f,0.95f,1f),dim)
        reset()
        batch.end()
    }

    private fun command(r: Rectangle,title:String,subtitle:String,titleColor:Color,subtitleColor:Color){
        val cx=r.x+r.width/2f
        fit(titleFont,title,r.width-44f,0.78f,0.55f); centered(titleFont,title,cx,r.y+r.height*0.66f,titleColor)
        fit(bodyFont,subtitle,r.width-44f,0.64f,0.44f); centered(bodyFont,subtitle,cx,r.y+r.height*0.27f,subtitleColor)
    }
    private fun fit(f:BitmapFont,t:String,max:Float,pref:Float,min:Float){f.data.setScale(1f);layout.setText(f,t);val ws=if(layout.width>0)max/layout.width else pref;f.data.setScale(minOf(pref,ws.coerceAtLeast(min),ws))}
    private fun centered(f:BitmapFont,t:String,x:Float,y:Float,c:Color){layout.setText(f,t);f.color=c;f.draw(batch,t,x-layout.width/2f,y)}
    private fun reset(){titleFont.data.setScale(1f);bodyFont.data.setScale(1f)}
}
