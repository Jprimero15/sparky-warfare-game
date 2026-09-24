// Local-only dev convenience: lets you iterate on the glow/laser VFX in a
// desktop window without a phone or emulator. Not part of the shipped
// game — the actual target platform is Android only (see android/).
plugins {
    kotlin("jvm")
}

val gdxVersion: String by project

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

tasks.register<JavaExec>("run") {
    dependsOn("classes")
    mainClass.set("com.sparkywarfare.game.desktop.DesktopLauncherKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    workingDir = rootProject.file("assets")
    isIgnoreExitValue = true
}
