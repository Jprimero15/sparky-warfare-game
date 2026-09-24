package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class ParticleDebris(private val batch: SpriteBatch) {
    private val template = ParticleEffect()
    private val active = mutableListOf<ParticleEffectPool.PooledEffect>()
    private val pool: ParticleEffectPool.PooledEffect

    init {
        template.load(
            Gdx.files.internal("particles/explosion.p"),
            Gdx.files.internal("particles")
        )
        pool = ParticleEffectPool(template, 4, 32)
    }

    fun spawn(position: Vector2) {
        val effect = pool.obtain()
        effect.setPosition(position.x, position.y)
        effect.start()
        active.add(effect)
    }

    fun update(delta: Float) {
        for (index in active.indices.reversed()) {
            val effect = active[index]
            effect.update(delta)
            if (effect.isComplete) {
                pool.free(effect)
                active.removeAt(index)
            }
        }
    }

    fun draw(projection: com.badlogic.gdx.math.Matrix4) {
        if (active.isEmpty()) return
        batch.projectionMatrix = projection
        batch.begin()
        for (effect in active) effect.draw(batch)
        batch.end()
    }

    fun clear() {
        for (effect in active) pool.free(effect)
        active.clear()
    }

    fun dispose() {
        clear()
        pool.clear()
        template.dispose()
    }
}
