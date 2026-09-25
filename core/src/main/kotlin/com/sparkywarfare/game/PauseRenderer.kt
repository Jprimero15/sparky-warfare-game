package com.sparkywarfare.game
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
class PauseRenderer(private val shape:ShapeRenderer,private val batch:SpriteBatch,private val font:BitmapFont){
fun draw(width:Float,height:Float,resume:Rectangle,menu:Rectangle,drawButton:(Rectangle,Color)->Unit,drawCentered:(String,Float,Float,Color)->Unit,fit:(String,Float,Float,Float)->Unit){
shape.begin(ShapeRenderer.ShapeType.Filled);shape.color=Color(0f,0f,0f,.78f);shape.rect(0f,0f,width,height);drawButton(resume,Color(.02f,.32f,.46f,.84f));drawButton(menu,Color(.04f,.06f,.08f,.9f));shape.end();batch.begin();fit("PAUSED",width*.5f,2f,1.2f);drawCentered("PAUSED",width/2f,height*.65f,Color(.55f,.92f,1f,1f));font.data.setScale(.72f);drawCentered("RESUME",resume.x+resume.width/2f,resume.y+resume.height/2f+9f,Color(.9f,.98f,1f,1f));drawCentered("MAIN MENU",menu.x+menu.width/2f,menu.y+menu.height/2f+9f,Color(.75f,.85f,.9f,1f));batch.end()
}}