# Sparky Warfare

A top-down tank combat game for **Android** — the grid, walls, and wave
structure of *Battle City / Tank 1990* as the gameplay skeleton,
reskinned as glowing energy tanks firing laser beams with a shared
additive-glow VFX system.

Built with **LibGDX + Kotlin**, targeting **Android only**, developed
**100% from the command line**, built automatically via
**GitHub Actions**.

## Status

A complete, playable loop:

- ✅ Menu → play → game over → retry, all through a tap or SPACE/ENTER
- ✅ Player movement + shooting, enemy tanks with basic chase-and-fire AI,
  destructible brick walls, steel walls, laser-vs-wall / laser-vs-tank
  collision
- ✅ Wave progression (each cleared wave spawns a harder one) + score
  (kills, power-up pickups) + on-screen health/score/wave HUD
- ✅ Power-up pickups (rapid-fire buff, score orb) that trigger the big
  "domain expansion" radial burst — the hero VFX moment from the
  original reference image
- ✅ Android touch controls: drag anywhere on the left half of the
  screen to move, tap/hold the right half to fire
- ⬜ Not yet built: sound, real bloom post-processing (the
  `.frag`/`.vert` shader files are included but not wired into the
  render pipeline — the current glow is the layered-circle approach in
  `GlowRenderer`), multiple arenas, real sprite art, more power-up
  types, persistent high scores, actual particle debris (the `.p`
  particle file exists but isn't wired into gameplay yet)

Treat the "not yet built" list as your natural next steps, not gaps in
what's here — the full loop already runs start to finish on a device.

## Project layout

```
sparky-warfare/
├── core/           # All game logic + rendering (Kotlin) — consumed by android/
│   └── .../game/
│       ├── SparkyWarfareGame.kt   # entry point
│       ├── GameScreen.kt          # states, waves, scoring, AI, collisions, touch input, HUD
│       ├── Tank.kt / Laser.kt / Wall.kt / PowerUp.kt
│       ├── VirtualJoystick.kt     # drag-based touch movement
│       └── GlowRenderer.kt        # THE shared VFX system — glow, beams, bursts, domain burst
├── android/        # The Android app — launcher, manifest, the only shipped module
├── assets/
│   ├── shaders/glow.vert, glow.frag   # bloom post-process shader (not yet wired in)
│   └── particles/explosion.p          # LibGDX ParticleEffect config (not yet wired in)
└── .github/workflows/build.yml    # CI: builds the debug APK on every push
```

`core` stays a plain Kotlin/JVM module (no Android SDK dependency) so
its logic and shaders are easy to reason about in isolation — but the
`android/` module is the only thing that actually builds and runs.
There's no desktop launcher; every test loop goes through a real
device, an emulator, or CI.

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

## Building it

You'll need a JDK (17+), the Android SDK, and Gradle (or Android
Studio, which bundles all three). This repo doesn't commit a Gradle
wrapper jar, so either:

```bash
# one-time, if you have Gradle installed locally:
gradle wrapper

# then use ./gradlew from here on:
./gradlew android:assembleDebug
```

Or just push to GitHub and let CI build it for you (see below) — that
was the point of going all-CLI.

## Running it

There's no desktop launcher, so testing means one of:

- **A physical Android device** — build with the command above, then
  `adb install android/build/outputs/apk/debug/android-debug.apk`
- **An Android emulator** (via `emulator` CLI or Android Studio's AVD
  Manager) — same install command, targets the emulator instead
- **GitHub Actions** — push and download the built APK (below), no
  local Android SDK setup at all

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

Touch only, matching the shipped Android target:

- Drag anywhere on the **left half** of the screen to move (virtual
  joystick)
- Tap or hold the **right half** of the screen to fire
- Tap anywhere to start from the menu, or retry after game over

(The keyboard-input code from earlier prototyping — WASD/arrows/Space —
is still present in `GameScreen.kt` as a harmless fallback if you're
debugging through an emulator with a hardware keyboard, but touch is
the actual supported input.)

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
