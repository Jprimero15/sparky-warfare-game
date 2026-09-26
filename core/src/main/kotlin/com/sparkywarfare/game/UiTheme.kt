package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color

/** Shared glassmorphic Soft Neon Arcade palette: luminous accents over translucent dark glass. */
object UiTheme {
    val PANEL = Color(0.028f, 0.045f, 0.095f, 0.74f)
    val PANEL_DARK = Color(0.010f, 0.020f, 0.055f, 0.82f)
    val INNER = Color(0.075f, 0.10f, 0.18f, 0.62f)
    val CARD = Color(0.050f, 0.075f, 0.145f, 0.72f)

    val CYAN = Color(0.22f, 0.96f, 1f, 1f)
    val CYAN_SOFT = Color(0.22f, 0.96f, 1f, 0.18f)
    val MAGENTA = Color(0.84f, 0.34f, 1f, 1f)
    val MAGENTA_SOFT = Color(0.84f, 0.34f, 1f, 0.17f)

    val DIM = Color(0.68f, 0.74f, 0.86f, 1f)
    val DIM_DARK = Color(0.45f, 0.52f, 0.65f, 1f)
    val WHITE = Color(0.95f, 0.985f, 1f, 1f)
    val DANGER = Color(1f, 0.28f, 0.48f, 1f)
    val GOLD = Color(1f, 0.74f, 0.3f, 1f)
    val BLACK = Color(0f, 0f, 0f, 1f)
    val SHADOW = Color(0f, 0f, 0f, 0.70f)

    val OVERLAY = Color(0.004f, 0.008f, 0.026f, 0.50f)
    val PANEL_EDGE = Color(0.30f, 0.94f, 1f, 0.22f)
    val GLASS_HIGHLIGHT = Color(1f, 1f, 1f, 0.085f)
    val GLASS_SHADOW = Color(0f, 0f, 0f, 0.22f)
    val CYAN_GLOW = Color(0.22f, 0.96f, 1f, 0.07f)
    val MAGENTA_GLOW = Color(0.84f, 0.34f, 1f, 0.06f)

    val ACCENT = CYAN
    val ACCENT_ALT = MAGENTA
    val TEXT_PRIMARY = WHITE
    val TEXT_SECONDARY = DIM
    val TEXT_MUTED = DIM_DARK

    object Metrics {
        const val PANEL_RADIUS = 32f
        const val INNER_RADIUS = 27f
        const val BUTTON_RADIUS = 24f
        const val CARD_RADIUS = 24f
        const val PANEL_INSET = 5f
        const val TOUCH_EXTRA_HIT = 32f
        const val BUTTON_TEXT_SCALE = 0.82f
        const val BUTTON_TEXT_MIN_SCALE = 0.56f
    }

    val TOUCH_BASE = Color(0.025f, 0.04f, 0.085f, 0.62f)
    val TOUCH_INNER = Color(0.055f, 0.085f, 0.15f, 0.60f)
    val TOUCH_KNOB_IDLE = Color(0.24f, 0.92f, 1f, 0.42f)
    val TOUCH_KNOB_ACTIVE = Color(0.24f, 0.92f, 1f, 0.72f)
    val MOVE_OUTLINE = Color(0.55f, 0.94f, 1f, 0.52f)
    val FIRE_BASE = Color(0.07f, 0.025f, 0.10f, 0.60f)
    val FIRE_IDLE = Color(0.92f, 0.34f, 1f, 0.34f)
    val FIRE_ACTIVE = Color(1f, 0.28f, 0.62f, 0.72f)
    val FIRE_OUTLINE = Color(1f, 0.58f, 0.92f, 0.56f)
}
