package com.sparkywarfare.game

import com.badlogic.gdx.Game

/** Entry point used by the Android launcher. */
class SparkyWarfareGame : Game() {
    override fun create() {
        setScreen(GameScreen())
    }
}
