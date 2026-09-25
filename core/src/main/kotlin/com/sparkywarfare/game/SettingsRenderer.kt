package com.sparkywarfare.game
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
class SettingsRenderer(private val shape:ShapeRenderer,private val batch:SpriteBatch,private val font:BitmapFont){
fun draw(width:Float,height:Float,safe:SafeArea,sfx:Rectangle,haptics:Rectangle,volume:Rectangle,swap:Rectangle,menu:Rectangle,muted:Boolean,hapticsMuted:Boolean,volumeValue:Float,drawButton:(Rectangle,Color)->Unit,drawCentered:(String,Float,Float,Color)->Unit,fit:(String,Float,Float,Float)->Unit){
shape.begin(ShapeRenderer.ShapeType.Filled);shape.color=Color(0f,0f,0f,.9f);shape.rect(0f,0f,width,height);drawButton(sfx,Color(.03f,.12f,.15f,.92f));drawButton(haptics,Color(.03f,.12f,.15f,.92f));shape.color=Color(.02f,.05f,.07f,.92f);shape.rect(volume.x,volume.y,volume.width,volume.height);shape.color=Color(.15f,.72f,1f,.8f);shape.rect(volume.x,volume.y,volume.width*volumeValue,volume.height);drawButton(swap,Color(.03f,.12f,.15f,.92f));drawButton(menu,Color(.03f,.12f,.15f,.92f));shape.end()
batch.begin();fit("SETTINGS",width*.6f,2f,1.2f);drawCentered("SETTINGS",(safe.left+safe.right)/2f,height*.72f,Color(.55f,.92f,1f,1f));font.data.setScale(.72f);drawCentered("SFX: "+if(muted)"MUTED" else "ON",sfx.x+sfx.width/2,sfx.y+sfx.height/2+9,Color(.85f,.95f,1f,1f));drawCentered("HAPTICS: "+if(hapticsMuted)"MUTED" else "ON",haptics.x+haptics.width/2,haptics.y+haptics.height/2+9,Color(.85f,.95f,1f,1f));font.data.setScale(.55f);drawCentered("MASTER VOLUME",volume.x+volume.width/2,volume.y+volume.height+18,Color(.55f,.7f,.75f,1f));font.data.setScale(.72f);drawCentered("SWAP MOVE / FIRE",swap.x+swap.width/2,swap.y+swap.height/2+9,Color(.85f,.95f,1f,1f));drawCentered("MAIN MENU",menu.x+menu.width/2,menu.y+menu.height/2+9,Color(.7f,.85f,.9f,1f));batch.end()
}}