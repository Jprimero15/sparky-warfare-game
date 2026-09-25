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
        UiShapes.roundedRect(shape,width*0.04f,height*0.06f,width*0.92f,height*0.88f,30f,UiTheme.PANEL)
        for(i in 0 until 3){
            val x=left+i*(cardW+gap)
            buttons[i].set(x,bottom,cardW,cardH)
            UiShapes.roundedRect(shape,x,bottom,cardW,cardH,22f,UiTheme.CARD)
            UiShapes.roundedRect(shape,x+10f,bottom+cardH-7f,cardW-20f,4f,2f,if(i==1)UiTheme.MAGENTA else UiTheme.CYAN)
        }
        shape.end()
        batch.begin()
        text.fit(titleFont,"UPGRADE PROTOCOL",width*0.72f,1.18f,0.76f)
        text.centered(titleFont,"UPGRADE PROTOCOL",cx,height*0.86f,UiTheme.CYAN)
        for(i in 0 until minOf(3,choices.size)){
            val r=buttons[i];val choice=choices[i]
            text.fit(titleFont,choice.title,r.width-28f,0.72f,0.48f)
            text.centered(titleFont,choice.title,r.x+r.width/2f,r.y+r.height-56f,Color.WHITE)
            text.fit(bodyFont,choice.description,r.width-28f,0.78f,0.52f)
            text.centered(bodyFont,choice.description,r.x+r.width/2f,r.y+r.height/2f,UiTheme.DIM)
        }
        text.reset(titleFont,bodyFont);batch.end()
    }
}
