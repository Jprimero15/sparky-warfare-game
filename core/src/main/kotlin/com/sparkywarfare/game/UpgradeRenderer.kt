package com.sparkywarfare.game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
class UpgradeRenderer(private val shape:ShapeRenderer,private val batch:SpriteBatch,private val font:BitmapFont){
fun draw(width:Float,height:Float,wave:Int,choices:List<UpgradeType>,buttons:Array<com.badlogic.gdx.math.Rectangle>,drawCentered:(String,Float,Float,Color)->Unit,fit:(String,Float,Float,Float)->Unit){
val cx=width/2f;val cw=(width*.27f).coerceIn(190f,300f);val ch=(height*.42f).coerceIn(210f,310f);val gap=(width*.025f).coerceIn(12f,28f);val left=cx-(cw*3f+gap*2f)/2f;val bottom=(height-ch)/2f-4f
Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);shape.begin(ShapeRenderer.ShapeType.Filled);shape.color=Color(0f,0f,0f,.92f);shape.rect(0f,0f,width,height);shape.color=Color(.03f,.16f,.2f,.2f);shape.rect(0f,height*.72f,width,height*.28f)
for(i in 0 until 3){val x=left+i*(cw+gap);buttons[i].set(x,bottom,cw,ch);shape.color=Color(.035f,.055f,.07f,.96f);shape.rect(x,bottom,cw,ch);shape.color=Color(.15f,.72f,1f,.7f);shape.rect(x,bottom+ch-3f,cw,3f)};shape.end();Gdx.gl.glDisable(GL20.GL_BLEND)
batch.begin();fit("CHOOSE YOUR UPGRADE",width*.72f,1.8f,1f);drawCentered("CHOOSE YOUR UPGRADE",cx,height*.86f,Color(.58f,.92f,1f,1f));font.data.setScale(.65f);drawCentered("WAVE $wave COMPLETE",cx,height*.79f,Color(.46f,.62f,.68f,1f))
for(i in 0 until minOf(3,choices.size)){val r=buttons[i];val choice=choices[i];fit(choice.title,r.width-24f,1.08f,.68f);drawCentered(choice.title,r.x+r.width/2f,r.y+r.height-52f,Color.WHITE);fit(choice.description,r.width-24f,.7f,.52f);drawCentered(choice.description,r.x+r.width/2f,r.y+r.height/2f+6f,Color(.45f,.72f,.8f,1f));font.data.setScale(.58f);drawCentered("TAP TO SELECT",r.x+r.width/2f,r.y+26f,Color(.38f,.52f,.58f,1f))};batch.end()
}}