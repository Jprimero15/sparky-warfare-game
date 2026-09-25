package com.sparkywarfare.game

class InputController {
    val joystick = VirtualJoystick()
    private val firePointers = HashSet<Int>(4)
    var firing = false
        private set
    var paused = false
        private set

    fun clearTransientInput() {
        firePointers.clear()
        firing = false
        joystick.reset()
    }

    fun setPaused(value: Boolean) {
        paused = value
        if (value) clearTransientInput()
    }

    fun pressFire(pointer: Int) {
        firePointers.add(pointer)
        firing = firePointers.isNotEmpty()
    }

    fun release(pointer: Int) {
        joystick.release(pointer)
        firePointers.remove(pointer)
        firing = firePointers.isNotEmpty()
    }

    fun drag(screenX: Int, screenY: Int, pointer: Int) {
        joystick.drag(screenX.toFloat(), screenY.toFloat(), pointer)
    }
}
