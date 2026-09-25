package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color

/** Canonical cyber-neon palette shared by game and overlay rendering. */
object UiTheme {
    val PANEL = Color(0.006f, 0.016f, 0.028f, 0.98f)
    val PANEL_DARK = Color(0.004f, 0.012f, 0.022f, 0.94f)
    val INNER = Color(0.018f, 0.038f, 0.06f, 0.97f)
    val CARD = Color(0.015f, 0.04f, 0.065f, 0.98f)
    val CYAN = Color(0.18f, 0.9f, 1f, 1f)
    val CYAN_SOFT = Color(0.18f, 0.9f, 1f, 0.22f)
    val MAGENTA = Color(0.78f, 0.24f, 1f, 1f)
    val MAGENTA_SOFT = Color(0.78f, 0.24f, 1f, 0.16f)
    val DIM = Color(0.46f, 0.64f, 0.7f, 1f)
    val DIM_DARK = Color(0.42f, 0.6f, 0.67f, 1f)
    val WHITE = Color(0.94f, 0.98f, 1f, 1f)
    val DANGER = Color(1f, 0.2f, 0.34f, 1f)
    val BLACK = Color(0f, 0f, 0f, 1f)
    val SHADOW = Color(0f, 0f, 0f, 0.8f)

    val TOUCH_BASE = Color(0.004f, 0.012f, 0.022f, 0.78f)
    val TOUCH_INNER = Color(0.02f, 0.06f, 0.09f, 0.92f)
    val TOUCH_KNOB_IDLE = Color(0.25f, 0.85f, 1f, 0.42f)
    val TOUCH_KNOB_ACTIVE = Color(0.25f, 0.85f, 1f, 0.7f)
    val MOVE_OUTLINE = Color(0.65f, 0.9f, 1f, 0.55f)
    val FIRE_BASE = Color(0.008f, 0.004f, 0.012f, 0.78f)
    val FIRE_IDLE = Color(1f, 0.25f, 0.34f, 0.4f)
    val FIRE_ACTIVE = Color(1f, 0.22f, 0.3f, 0.76f)
    val FIRE_OUTLINE = Color(1f, 0.65f, 0.7f, 0.65f)
}