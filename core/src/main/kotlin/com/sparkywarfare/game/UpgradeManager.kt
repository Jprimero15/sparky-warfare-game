package com.sparkywarfare.game

import com.badlogic.gdx.math.MathUtils

/** Owns upgrade selection, stack limits, and player upgrade effects. */
class UpgradeManager {
    val choices = mutableListOf<UpgradeType>()
    private val levels = mutableMapOf<UpgradeType, Int>()

    fun clear() {
        choices.clear()
        levels.clear()
    }

    fun prepareChoices() {
        choices.clear()
        val pool = UpgradeType.values().filter { (levels[it] ?: 0) < it.maxStacks }.toMutableList()
        while (choices.size < 3 && pool.isNotEmpty()) {
            choices.add(pool.removeAt(MathUtils.random(pool.size - 1)))
        }
    }

    fun apply(type: UpgradeType, player: Tank, currentScoreMultiplier: Int): Int {
        levels[type] = (levels[type] ?: 0) + 1
        return when (type) {
            UpgradeType.OVERCLOCK -> {
                player.fireRate = (player.fireRate * GameConfig.PowerUps.OVERCLOCK_FACTOR)
                    .coerceAtLeast(GameConfig.PowerUps.UPGRADE_FIRE_FLOOR)
                currentScoreMultiplier
            }
            UpgradeType.THRUSTERS -> {
                player.speed *= 1.15f
                currentScoreMultiplier
            }
            UpgradeType.REPAIR -> {
                player.health = (player.health + 1).coerceAtMost(player.maxHealth)
                currentScoreMultiplier
            }
            UpgradeType.SCORE_CORE -> (currentScoreMultiplier + 1).coerceAtMost(3)
            UpgradeType.ARMOR -> {
                player.maxHealth = (player.maxHealth + 1).coerceAtMost(GameConfig.Player.MAX_HP + 2)
                player.health = (player.health + 1).coerceAtMost(player.maxHealth)
                currentScoreMultiplier
            }
            UpgradeType.COOLING -> {
                player.fireRate = (player.fireRate * GameConfig.PowerUps.COOLING_FACTOR)
                    .coerceAtLeast(GameConfig.PowerUps.UPGRADE_FIRE_FLOOR)
                currentScoreMultiplier
            }
            UpgradeType.ENERGY_CELL -> {
                player.grantShield(4.5f)
                currentScoreMultiplier
            }
            UpgradeType.OVERDRIVE_CORE -> {
                player.grantOverdrive(5.5f)
                player.grantRapidFire(5.5f)
                currentScoreMultiplier
            }
        }
    }
}
