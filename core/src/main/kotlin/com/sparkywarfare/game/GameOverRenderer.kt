package com.sparkywarfare.game
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
class GameOverRenderer(private val shape:ShapeRenderer,private val batch:SpriteBatch,private val font:BitmapFont){
fun draw(width:Float,height:Float,safe:SafeArea,retry:Rectangle,menu:Rectangle,score:Int,best:Int,wave:Int,kills:Int,combo:Int,drawButton:(Rectangle,Color)->Unit,drawCentered:(String,Float,Float,Color)->Unit,fit:(String,Float,Float,Float)->Unit){
shape.begin(ShapeRenderer.ShapeType.Filled);shape.color=Color(0f,0f,0f,.9f);shape.rect(0f,0f,width,height);shape.color=Color(.14f,.015f,.025f,.82f);shape.rect(width*.14f,height*.24f,width*.72f,height*.52f);shape.end()
batch.begin();fit("SYSTEM DOWN",width*.6f,2.2f,1.2f);drawCentered("SYSTEM DOWN",width/2,height*.7f,Color(1f,.2f,.28f,1f));font.data.setScale(.72f);drawCentered("SCORE  $score     BEST  $best",width/2,height*.58f,Color(.9f,.95f,1f,1f));drawCentered("WAVE  $wave     KILLS  $kills     COMBO  x$combo",width/2,height*.5f,Color(.55f,.7f,.75f,1f));drawButton(retry,Color(.02f,.32f,.46f,.82f));drawButton(menu,Color(.04f,.06f,.08f,.88f));font.data.setScale(.7f);drawCentered("RETRY",retry.x+retry.width/2,retry.y+retry.height/2+9,Color(.9f,.98f,1f,1f));drawCentered("MAIN MENU",menu.x+menu.width/2,menu.y+menu.height/2+9,Color(.75f,.85f,.9f,1f));batch.end()
}}