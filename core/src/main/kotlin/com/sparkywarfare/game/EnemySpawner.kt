package com.sparkywarfare.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class EnemySpawner {
    private val candidate = Vector2()

    fun spawnWave(wave: Int, player: Tank, walls: List<Wall>, enemies: MutableList<Tank>) {
        val waveManager = WaveManager()
        val count = waveManager.enemyCount(wave)
        val spawnY = (player.position.y + GameConfig.VIEW_HEIGHT * 0.65f).coerceAtMost(GameConfig.WORLD_HEIGHT - 70f)
        val left = (player.position.x - GameConfig.VIEW_WIDTH * 0.9f).coerceAtLeast(70f)
        val right = (player.position.x + GameConfig.VIEW_WIDTH * 0.9f).coerceAtMost(GameConfig.WORLD_WIDTH - 70f)
        val spacing = ((right - left) / (count + 1)).coerceAtLeast(48f)

        for (i in 0 until count) {
            val elite = waveManager.isEliteWave(wave) && i == count - 1
            val role = (i + wave) % 4
            val enemy = createEnemy(wave, role, elite)
            val preferredX = (left + spacing * (i + 1)).coerceIn(60f, GameConfig.WORLD_WIDTH - 60f)
            if (findSafePosition(preferredX, spawnY, enemy, player, walls, enemies)) {
                enemy.position.set(candidate)
                enemy.aiFireTimer = MathUtils.random(GameConfig.Enemy.AI_MIN_FIRE_DELAY, GameConfig.Enemy.AI_MAX_FIRE_DELAY)
                enemies.add(enemy)
            }
        }
    }

    private fun createEnemy(wave: Int, role: Int, elite: Boolean): Tank {
        if (elite) return Tank(Vector2(), 270f, false, Color(1f, 0.78f, 0.12f, 1f),
            (48f + wave).coerceAtMost(72f), 6 + (wave - 5) / 3, 0f,
            (0.72f - wave * 0.009f).coerceAtLeast(0.46f), 22f)
        return when (role) {
            0 -> Tank(Vector2(), 270f, false, Color(1f, 0.25f, 0.35f, 1f), (78f + wave * 1.5f).coerceAtMost(108f), 1 + (wave - 1) / 6, 0f, (1.35f - wave * 0.018f).coerceAtLeast(0.78f), 11f)
            1 -> Tank(Vector2(), 270f, false, Color(1f, 0.42f, 0.18f, 1f), (58f + wave * 2f).coerceAtMost(90f), 1 + (wave - 1) / 5, 0f, (1.1f - wave * 0.022f).coerceAtLeast(0.58f), 14f)
            2 -> Tank(Vector2(), 270f, false, Color(0.95f, 0.16f, 0.55f, 1f), (42f + wave * 1.15f).coerceAtMost(66f), 3 + (wave - 1) / 4, 0f, (1.45f - wave * 0.018f).coerceAtLeast(0.84f), 18f)
            else -> Tank(Vector2(), 270f, false, Color(0.72f, 0.28f, 1f, 1f), (48f + wave * 1.15f).coerceAtMost(74f), 2 + (wave - 1) / 5, 0f, (0.95f - wave * 0.016f).coerceAtLeast(0.54f), 12f)
        }
    }

    private fun findSafePosition(preferredX: Float, preferredY: Float, enemy: Tank, player: Tank, walls: List<Wall>, enemies: List<Tank>): Boolean {
        repeat(GameConfig.Enemy.SPAWN_ATTEMPTS) { attempt ->
            if (attempt == 0) candidate.set(preferredX, preferredY)
            else candidate.set(MathUtils.random(60f, GameConfig.WORLD_WIDTH - 60f), MathUtils.random(60f, GameConfig.WORLD_HEIGHT - 60f))
            if (candidate.dst2(player.position) < GameConfig.Enemy.MIN_SPAWN_DISTANCE * GameConfig.Enemy.MIN_SPAWN_DISTANCE) return@repeat
            if (walls.any { it.alive && CollisionSystem.circleIntersectsRectangle(candidate, enemy.radius, it.bounds) }) return@repeat
            if (enemies.any { it.alive && candidate.dst2(it.position) < (enemy.radius + it.radius + 10f) * (enemy.radius + it.radius + 10f) }) return@repeat
            return true
        }
        return false
    }
}
