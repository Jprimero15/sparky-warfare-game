package com.sparkywarfare.game

import com.badlogic.gdx.Game

/** Entry point shared by desktop and Android launchers. */
class SparkyWarfareGame : Game() {
    override fun create() {
        setScreen(GameScreen())
    }
}
