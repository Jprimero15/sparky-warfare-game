package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

class ParticleDebris(private val batch: SpriteBatch) {
    private data class Debris(
        val position: Vector2,
        val velocity: Vector2,
        var life: Float,
        val maxLife: Float,
        val size: Float,
        val rotation: Float,
        val rotationSpeed: Float
    )

    private val texture = Texture(Gdx.files.internal("particles/particle.png"))
    private val active = ArrayList<Debris>(64)

    fun spawn(position: Vector2) {
        val count = 12
        repeat(count) { index ->
            val angle = index * (360f / count) + ((index * 37) % 19)
            val radians = Math.toRadians(angle.toDouble()).toFloat()
            val speed = 45f + ((index * 29) % 80)
            val life = 0.28f + ((index * 17) % 25) / 100f
            active.add(
                Debris(
                    position = Vector2(position.x, position.y),
                    velocity = Vector2(cos(radians) * speed, sin(radians) * speed),
                    life = life,
                    maxLife = life,
                    size = 3f + ((index * 11) % 5),
                    rotation = angle,
                    rotationSpeed = -180f + ((index * 23) % 360)
                )
            )
        }
    }

    fun update(delta: Float) {
        val step = delta.coerceIn(0f, 0.05f)
        for (index in active.indices.reversed()) {
            val debris = active[index]
            debris.life -= step
            if (debris.life <= 0f) {
                active.removeAt(index)
                continue
            }
            debris.position.mulAdd(debris.velocity, step)
            debris.velocity.scl(0.94f)
            debris.velocity.y -= 28f * step
        }
    }

    fun draw(projection: Matrix4) {
        if (active.isEmpty()) return
        batch.projectionMatrix = projection
        batch.begin()
        for (debris in active) {
            val alpha = (debris.life / debris.maxLife).coerceIn(0f, 1f)
            batch.setColor(1f, 0.65f + 0.35f * alpha, 0.18f, alpha)
            val size = debris.size * (0.65f + 0.35f * alpha)
            batch.draw(
                texture,
                debris.position.x - size * 0.5f,
                debris.position.y - size * 0.5f,
                size * 0.5f,
                size * 0.5f,
                size,
                size,
                1f,
                1f,
                debris.rotation + debris.rotationSpeed * (debris.maxLife - debris.life),
                0,
                0,
                texture.width,
                texture.height,
                false,
                false
            )
        }
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    fun clear() {
        active.clear()
    }

    fun dispose() {
        active.clear()
        texture.dispose()
    }
}
