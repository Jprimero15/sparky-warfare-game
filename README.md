# Sparky Warfare

A top-down Android tank-combat game built with **LibGDX + Kotlin**. The game uses glowing energy tanks, laser fire, destructible walls, waves, power-ups, and Android touch controls.

## Status

The complete gameplay loop is playable:

- Menu → play → game over → retry
- Player movement and continuous firing
- Enemy chase-and-fire AI
- Destructible brick walls and indestructible steel walls
- Wave progression and scoring
- Rapid-fire and score power-ups
- Android multitouch movement/fire controls

Recent stability fixes harden the gameplay loop:

- Tank movement uses a circular footprint instead of a center-point wall test.
- Player and enemies are kept separated and clamped inside the arena.
- Enemy firing is timer-based instead of frame-rate-dependent random firing.
- Waves cap the enemy count while increasing speed, health, and firing difficulty.
- Laser collision uses the travelled segment, preventing fast shots from skipping targets or walls.
- Restart clears joystick and fire-pointer state so an old touch cannot remain stuck.
- Large frame deltas are capped to avoid physics jumps after a stalled frame.
- Power-ups avoid walls, the player spawn area, and duplicate live pickups.
- Input is cleared when the screen is disposed.

## Project layout

```
core/           # Game logic and rendering
android/        # Android application
assets/         # Shaders and particle resources
.github/        # GitHub Actions CI
```

Important gameplay files are under `core/src/main/kotlin/com/sparkywarfare/game/`:

- `GameScreen.kt` — states, waves, AI, movement, collisions, touch input and HUD
- `Tank.kt` — health, cooldowns and rapid-fire state
- `Laser.kt` — laser movement and swept-collision history
- `VirtualJoystick.kt` — multitouch movement control
- `GlowRenderer.kt` — tank, laser and burst rendering

## Controls

On Android:

- Drag on the **left half** of the screen to move.
- Tap/hold the **right half** to fire.
- Tap anywhere on the menu to start.
- Tap anywhere after game over to restart.

Keyboard fallback:

- WASD / arrow keys to move.
- SPACE to fire or start/restart.

## Building

With Gradle installed:

```bash
gradle :android:assembleDebug --no-daemon
```

The APK is generated at:

```
android/build/outputs/apk/debug/
```

## GitHub Actions

`.github/workflows/build.yml` builds the Android debug APK on pushes to `main`, pull requests, and manual runs.

CI uses:

- JDK 17
- Android platform API 34
- Android build-tools 34.0.0
- Gradle Actions setup v4

The workflow deliberately installs specific SDK packages rather than relying on the obsolete `tools` SDK package that previously caused setup failures.

The generated `sparky-warfare-debug` artifact contains the debug APK.

## Rendering

`GlowRenderer` provides the shared layered glow style for tanks, lasers, power-ups and bursts. The included shader and particle resources remain available for a future framebuffer bloom/particle pass.

## Future work

1. Real bloom via the included framebuffer shader.
2. Particle debris using the included LibGDX particle resource.
3. Sound effects for firing, impacts and pickups.
4. Additional arena layouts and power-up types.
5. Persistent high scores.
6. More advanced enemy behavior.
