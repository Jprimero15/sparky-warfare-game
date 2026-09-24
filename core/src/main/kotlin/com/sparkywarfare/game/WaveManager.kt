package com.sparkywarfare.game

class WaveManager {
    fun enemyCount(wave: Int): Int = (2 + wave).coerceAtMost(GameConfig.Enemy.MAX_PER_WAVE)
    fun isEliteWave(wave: Int): Boolean = wave > 0 && wave % 5 == 0
    fun spawnDelay(wave: Int): Float = (1.2f - wave * 0.025f).coerceAtLeast(0.35f)
}
