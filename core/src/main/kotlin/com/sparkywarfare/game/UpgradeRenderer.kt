package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class UpgradeRenderer(
    private val shape: ShapeRenderer,
    private val batch: SpriteBatch,
    private val titleFont: BitmapFont,
    private val bodyFont: BitmapFont,
    private val text: UiText
) {
    fun draw(width: Float,height: Float,wave: Int,choices: List<UpgradeType>,buttons: Array<Rectangle>) {
        val cx=width/2f
        val availableWidth=width*0.9f
        val gap=(width*0.022f).coerceIn(12f,28f)
        val cardW=((availableWidth-gap*2f)/3f).coerceIn(180f,320f)
        val cardH=(height*0.48f).coerceIn(230f,330f)
        val totalW=cardW*3f+gap*2f
        val left=cx-totalW/2f
        val bottom=(height*0.18f).coerceAtLeast(82f)
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=UiTheme.BLACK;shape.color.a=0.88f;shape.rect(0f,0f,width,height)
        shape.color=UiTheme.PANEL;shape.rect(width*0.04f,height*0.06f,width*0.92f,height*0.88f)
        shape.color=UiTheme.CYAN;shape.rect(width*0.04f,height*0.94f-3f,width*0.92f,3f)
        shape.color=UiTheme.MAGENTA;shape.rect(width*0.96f-4f,height*0.94f-34f,4f,31f)
        for(i in 0 until 3){
            val x=left+i*(cardW+gap)
            buttons[i].set(x,bottom,cardW,cardH)
            shape.color=UiTheme.CARD;shape.rect(x,bottom,cardW,cardH)
            shape.color=if(i==1)UiTheme.MAGENTA else UiTheme.CYAN;shape.rect(x,bottom+cardH-4f,cardW,4f)
            shape.color=UiTheme.CYAN_SOFT;shape.rect(x+8f,bottom+8f,3f,cardH-16f)
        }
        shape.end()
        batch.begin()
        text.fit(titleFont,"UPGRADE PROTOCOL",width*0.72f,1.18f,0.76f)
        text.centered(titleFont,"UPGRADE PROTOCOL",cx,height*0.86f,UiTheme.CYAN)
        val subtitle="WAVE $wave COMPLETE // SELECT ONE SYSTEM MOD"
        text.fit(bodyFont,subtitle,width*0.78f,0.82f,0.56f)
        text.centered(bodyFont,subtitle,cx,height*0.79f,UiTheme.DIM)
        for(i in 0 until minOf(3,choices.size)){
            val r=buttons[i];val choice=choices[i]
            text.fit(titleFont,choice.title,r.width-28f,0.72f,0.48f)
            text.centered(titleFont,choice.title,r.x+r.width/2f,r.y+r.height-56f,Color.WHITE)
            text.fit(bodyFont,choice.description,r.width-28f,0.78f,0.52f)
            text.centered(bodyFont,choice.description,r.x+r.width/2f,r.y+r.height/2f+5f,UiTheme.DIM)
            text.fit(bodyFont,"TAP TO INSTALL",r.width-30f,0.62f,0.44f)
            text.centered(bodyFont,"TAP TO INSTALL",r.x+r.width/2f,r.y+27f,if(i==1)UiTheme.MAGENTA else UiTheme.CYAN)
        }
        text.reset(titleFont,bodyFont);batch.end()
    }
}
