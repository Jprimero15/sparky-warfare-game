package com.sparkywarfare.game.ui
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
data class SafeArea(val left:Float,val bottom:Float,val right:Float,val top:Float){
    fun rect():Rectangle=Rectangle(left,bottom,right-left,top-bottom)
    companion object{
        fun fromScreen(width:Float,height:Float):SafeArea{
            val l=Gdx.graphics.safeInsetLeft.toFloat()
            val r=Gdx.graphics.safeInsetRight.toFloat()
            val t=Gdx.graphics.safeInsetTop.toFloat()
            val b=Gdx.graphics.safeInsetBottom.toFloat()
            return SafeArea(l,b,width-r,height-t)
        }
    }
}