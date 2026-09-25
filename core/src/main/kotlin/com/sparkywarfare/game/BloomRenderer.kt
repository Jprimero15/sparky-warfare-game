package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram

class BloomRenderer {
    private var frameBuffer: FrameBuffer? = null
    private val camera = OrthographicCamera()
    private val batch = SpriteBatch()
    private var shader: ShaderProgram? = null
    private var region: TextureRegion? = null
    private var width = 1
    private var height = 1

    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (this.width == width && this.height == height && frameBuffer != null) return
        frameBuffer?.dispose()
        this.width = width
        this.height = height
        frameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        region = TextureRegion(frameBuffer!!.colorBufferTexture).also { it.flip(false, true) }
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        shader?.bind()
        shader?.setUniformf("u_texelSize", 1f / width.toFloat(), 1f / height.toFloat())
        camera.update()
    }

    fun begin() {
        val fbo = frameBuffer ?: return
        fbo.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    fun endAndComposite() {
        val fbo = frameBuffer ?: return
        fbo.end()
        val currentShader = shader ?: return
        batch.projectionMatrix = camera.combined
        batch.setShader(currentShader)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)
        batch.draw(region!!, 0f, 0f, width.toFloat(), height.toFloat())
        batch.end()
        batch.setShader(null)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun dispose() {
        frameBuffer?.dispose()
        frameBuffer = null
        region = null
        shader?.dispose()
        shader = null
        batch.dispose()
    }

    init {
        ShaderProgram.pedantic = false
        val candidate = ShaderProgram(
            Gdx.files.internal("shaders/glow.vert"),
            Gdx.files.internal("shaders/glow.frag")
        )
        if (candidate.isCompiled) {
            shader = candidate
            shader?.bind()
            shader?.setUniformi("u_texture", 0)
            shader?.setUniformf("u_intensity", 0.9f)
        } else {
            Gdx.app.error("BloomRenderer", "Bloom shader failed to compile: " + candidate.log)
            candidate.dispose()
        }
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }
}
