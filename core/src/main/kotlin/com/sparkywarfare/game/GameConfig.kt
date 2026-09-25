package com.sparkywarfare.game

object GameConfig {
    const val WORLD_WIDTH = 1600f
    const val WORLD_HEIGHT = 900f
    const val TILE = 32f
    const val VIEW_WIDTH = 480f
    const val VIEW_HEIGHT = 270f

    object Player {
        const val SPEED = 115f
        const val MAX_HP = 4
        const val START_HP = 3
        const val RADIUS = 14f
        const val FIRE_RATE = 0.28f
        const val HIT_IFRAMES = 0.65f
    }

    object Enemy {
        const val MAX_PER_WAVE = 10
        const val MIN_SPAWN_DISTANCE = 190f
        const val SPAWN_ATTEMPTS = 32
        const val AI_MIN_FIRE_DELAY = 0.85f
        const val AI_MAX_FIRE_DELAY = 1.7f
        const val AI_MIN_REACTION_DELAY = 0.18f
        const val AI_MAX_REACTION_DELAY = 0.55f
        const val AI_MAX_AIM_TURN_SPEED = 105f
        const val AI_FIRE_ANGLE_TOLERANCE = 12f
        const val AI_SCOUT_SPREAD = 13f
        const val AI_ASSAULT_SPREAD = 9f
        const val AI_HEAVY_SPREAD = 6f
        const val AI_RANGED_SPREAD = 15f
        const val AI_ELITE_SPREAD = 5f
    }

    object Combat {
        const val LASER_SPEED = 420f
        const val LASER_LENGTH = 18f
        const val LASER_LIFE = 1.4f
        const val MAX_COMBO = 8
        const val COMBO_TIMEOUT = 3f
        const val TANK_SEPARATION = 30f
    }

    object PowerUps {
        const val PICKUP_RADIUS = 20f
        const val RAPID_FIRE_DURATION = 6f
        const val SHIELD_DURATION = 5f
        const val SPREAD_DURATION = 7f
        const val OVERDRIVE_DURATION = 6f
    }

    object Ui {
        const val SAFE_MARGIN = 18f
        const val BUTTON_GAP = 12f
        const val TRANSITION_SPEED = 4.5f
        const val PANEL_ALPHA = 0.9f
    }

    object Waves {
        const val ELITE_INTERVAL = 5
        const val UPGRADE_INTERVAL = 3
        const val ELITE_BONUS = 500
    }
}
