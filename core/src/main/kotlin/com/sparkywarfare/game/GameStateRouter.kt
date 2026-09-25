package com.sparkywarfare.game

/** Owns state transitions and the shared fade transition used by GameScreen. */
class GameStateRouter {
    var state = GameState.MENU
        private set
    var transition = 1f
        private set

    private var transitionTarget = 1f

    fun update(delta: Float) {
        transition += (transitionTarget - transition) *
            (delta * GameConfig.Ui.TRANSITION_SPEED).coerceAtMost(1f)
    }

    fun beginFadeIn() {
        transition = 1f
        transitionTarget = 0f
    }

    fun goTo(next: GameState, fadeIn: Boolean = false) {
        state = next
        if (fadeIn) beginFadeIn() else {
            transition = 0f
            transitionTarget = 0f
        }
    }

    fun goBack(): GameState? {
        val next = when (state) {
            GameState.PLAYING -> GameState.PAUSED
            GameState.PAUSED -> GameState.PLAYING
            GameState.SETTINGS -> GameState.MENU
            GameState.GAME_OVER, GameState.UPGRADE -> GameState.MENU
            GameState.MENU -> return null
        }
        goTo(next)
        return next
    }
}