package com.sparkywarfare.game

enum class EnemyTier(val scoreValue: Int) {
    SCOUT(125),
    ASSAULT(175),
    RANGED(175),
    HEAVY(250),
    ELITE(500)
}