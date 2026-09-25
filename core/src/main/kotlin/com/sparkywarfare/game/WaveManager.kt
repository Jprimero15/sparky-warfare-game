package com.sparkywarfare.game

class WaveManager {
    fun enemyCount(wave: Int): Int {
        val pressure = (wave - 1).coerceAtLeast(0)
        return (2 + pressure + pressure / 4).coerceAtMost(GameConfig.Enemy.MAX_PER_WAVE)
    }

    fun isEliteWave(wave: Int): Boolean =
        wave > 0 && wave % GameConfig.Waves.ELITE_INTERVAL == 0

    fun isUpgradeWave(wave: Int): Boolean =
        wave > 0 && wave % GameConfig.Waves.UPGRADE_INTERVAL == 0

    fun eliteBonus(wave: Int): Int =
        if (isEliteWave(wave)) GameConfig.Waves.ELITE_BONUS else 0
}
