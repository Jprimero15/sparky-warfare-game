# Sparky Warfare

A top-down tank combat game — the grid, walls, and wave structure of
*Battle City / Tank 1990* as the gameplay skeleton, reskinned as glowing
energy tanks firing laser beams with a shared additive-glow VFX system.

Built with **LibGDX + Kotlin**, developed **100% from the command line**,
built automatically via **GitHub Actions**.

**Target platform: Android only.** The `android/` module is the actual
product. `desktop/` exists purely as a local dev convenience — it lets
you test movement, shooting, and the glow VFX in a window on your own
machine without a phone or emulator — but it is not part of what ships.

## Status

A complete, playable loop:

- ✅ Menu → play → game over → retry, all through tap or SPACE/ENTER
- ✅ Player movement + shooting, enemy tanks with basic chase-and-fire AI,
  destructible brick walls, steel walls, laser-vs-wall / laser-vs-tank
  collision
- ✅ Wave progression (each cleared wave spawns a harder one) + score
  (kills, power-up pickups) + on-screen health/score/wave HUD
- ✅ Power-up pickups (rapid-fire buff, score orb) that trigger the big
  "domain expansion" radial burst — the hero VFX moment from the
  original reference image
- ✅ Android touch controls: drag anywhere on the left half to move,
  tap/hold the right half to fire — alongside WASD/arrows + Space for
  desktop testing
- ⬜ Not yet built: sound, real bloom post-processing (the
  `.frag`/`.vert` shader files are included but not wired into the
  render pipeline — the current glow is the layered-circle approach in
  `GlowRenderer`), multiple arenas, real sprite art, more power-up
  types, persistent high scores

Treat the "not yet built" list as your natural next steps, not gaps in
what's here — the full loop already runs start to finish.

## Project layout

```
sparky-warfare/
├── core/           # Shared game logic + rendering (Kotlin) — all platforms use this
│   └── .../game/
│       ├── SparkyWarfareGame.kt   # entry point
│       ├── GameScreen.kt          # states, waves, scoring, AI, collisions, touch input, HUD
│       ├── Tank.kt / Laser.kt / Wall.kt / PowerUp.kt
│       ├── VirtualJoystick.kt     # drag-based touch movement
│       └── GlowRenderer.kt        # THE shared VFX system — glow, beams, bursts, domain burst
├── desktop/        # Dev-only: fast local VFX testing, not shipped
├── android/        # THE product — Android launcher + manifest
├── assets/
│   ├── shaders/glow.vert, glow.frag   # bloom post-process shader (not yet wired in)
│   └── particles/explosion.p          # LibGDX ParticleEffect config
└── .github/workflows/build.yml    # CI: builds the debug APK on every push
```

## The VFX approach

Every glowing thing in the game — tank cores, laser beams, explosion
bursts — goes through **one** class: `GlowRenderer`. It draws everything
with **additive blending** (`GL_SRC_ALPHA, GL_ONE`), so overlapping glows
brighten instead of muddying, which is the core trick behind this whole
visual style. Extend that one file rather than writing a new effect
system per entity.

The included `.frag`/`.vert` shaders are a next-step upgrade path: a
luminance-threshold bloom pass you can apply to a `FrameBuffer` render
of the whole scene for a "real" glow halo, instead of (or in addition
to) the current layered-circle approximation in `GlowRenderer`.

## Running it locally

You'll need a JDK (17+) and Gradle installed (or Android Studio, which
bundles both). This repo doesn't commit a Gradle wrapper jar, so either:

```bash
# one-time, if you have Gradle installed locally:
gradle wrapper

# then use ./gradlew from here on, e.g.:
./gradlew desktop:run          # fast iteration, runs on your machine
./gradlew android:assembleDebug # builds an APK locally
```

Or just push to GitHub and let CI build it for you (see below) — that
was the point of going all-CLI.

## GitHub Actions (the actual "100% CLI" workflow)

`.github/workflows/build.yml` runs on every push to `main` and every
pull request:

1. Checks out the repo
2. Installs JDK 17 + the Android SDK
3. Installs Gradle via `gradle/actions/setup-gradle` (no wrapper jar
   needed)
4. Runs `gradle android:assembleDebug`
5. Uploads the resulting `.apk` as a workflow artifact

**Your loop becomes:**

```bash
git add .
git commit -m "add laser trail glow"
git push
```

Then open the **Actions** tab on GitHub, wait for the green check,
download the `sparky-warfare-debug` artifact, and:

```bash
adb install sparky-warfare-debug.apk
```

No local Android SDK setup ever required.

## Controls

- **Desktop (dev testing):** WASD/arrow keys to move, Space to fire,
  Space/Enter to start or retry from the menu/game-over screen
- **Android:** drag anywhere on the left half of the screen to move
  (virtual joystick), tap/hold the right half to fire, tap anywhere to
  start or retry

## Next steps, roughly in order

1. Wire the bloom shader (`glow.frag`) into a `FrameBuffer` pass over
   the whole scene for a real halo instead of the layered-circle glow
2. Swap `explosion.p` in via LibGDX's `ParticleEffect` API alongside
   `GlowRenderer.drawBurst` for denser explosion debris
3. Sound effects (laser fire, explosions, power-up pickup) via
   `Gdx.audio`
4. More power-up types, a second arena layout, persistent high score
   (LibGDX `Preferences`)
5. Replace the AI's basic chase-and-fire in `updateEnemyAi` with
   proper states (patrol/attack/retreat) if you want smarter enemies
