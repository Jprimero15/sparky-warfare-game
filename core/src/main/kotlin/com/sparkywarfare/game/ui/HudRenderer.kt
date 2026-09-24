package com.sparkywarfare.game.ui
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
class HudRenderer(private val font:BitmapFont, private val batch:SpriteBatch, private val layout:GlyphLayout){
    fun measure(text:String):Float{layout.setText(font,text);return layout.width}
    fun shadow(text:String,x:Float,y:Float,color:Color){font.color=Color(0f,0f,0f,.8f);font.draw(batch,text,x+1.5f,y-1.5f);font.color.set(color);font.draw(batch,text,x,y)}
}