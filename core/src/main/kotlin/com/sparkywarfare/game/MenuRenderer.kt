package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MenuRenderer(private val shape: ShapeRenderer, private val batch: SpriteBatch, private val font: BitmapFont) {
    fun draw(width: Float, height: Float, safe: SafeArea, single: Rectangle, multi: Rectangle, settings: Rectangle, drawButton: (Rectangle, Color) -> Unit, drawCentered: (String, Float, Float, Color) -> Unit, fit: (String, Float, Float, Float) -> Unit) {
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color=Color(0f,0f,0f,.9f); shape.rect(0f,0f,width,height)
        shape.color=Color(.03f,.16f,.2f,.22f); shape.rect(0f,height*.78f,width,height*.22f)
        drawButton(single,Color(.02f,.32f,.46f,.78f)); drawButton(multi,Color(.055f,.065f,.075f,.86f)); drawButton(settings,Color(.03f,.12f,.15f,.92f)); shape.end()
        batch.begin(); fit("SPARKY WARFARE",width*.78f,2.4f,1.4f); drawCentered("SPARKY WARFARE",(safe.left+safe.right)/2f,height*.77f,Color(.45f,.9f,1f,1f)); font.data.setScale(.8f); drawCentered("TACTICAL ENERGY COMBAT",(safe.left+safe.right)/2f,height*.71f,Color(.5f,.62f,.68f,1f)); font.data.setScale(.75f); drawCentered("SINGLE PLAYER",single.x+single.width/2f,single.y+single.height/2f+9f,Color(.9f,.98f,1f,1f)); drawCentered("MULTIPLAYER  •  COMING SOON",multi.x+multi.width/2f,multi.y+multi.height/2f+9f,Color(.55f,.62f,.66f,1f)); drawCentered("SETTINGS",settings.x+settings.width/2f,settings.y+settings.height/2f+9f,Color(.7f,.9f,.95f,1f)); batch.end()
    }
}