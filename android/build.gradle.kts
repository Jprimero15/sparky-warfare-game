import java.io.File
import java.util.zip.ZipFile

// Android-only application module for Sparky Warfare.
plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by project

android {
    namespace = "com.sparkywarfare.game.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sparkywarfare.game"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs(rootProject.file("assets"))
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes.add("META-INF/robovm/ios/robovm.xml")
    }
}

val gdxNatives by configurations.creating

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    gdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    gdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    gdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    gdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}

val extractGdxNatives = tasks.register("extractGdxNatives") {
    val outputDir = layout.buildDirectory.dir("generated/gdxNatives")
    outputs.dir(outputDir)

    doLast {
        val destination = outputDir.get().asFile
        destination.deleteRecursively()
        destination.mkdirs()

        val abis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        val nativeFiles: Set<File> = configurations.getByName("gdxNatives").files

        for (jarFile in nativeFiles) {
            ZipFile(jarFile).use { zipFile ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.isDirectory || !entry.name.endsWith(".so")) continue

                    var abi: String? = null
                    for (abiName in abis) {
                        if (entry.name.contains("/" + abiName + "/") ||
                            entry.name.contains(abiName + "/")) {
                            abi = abiName
                            break
                        }
                    }

                    if (abi != null) {
                        val output = destination.resolve(
                            abi + "/" + entry.name.substringAfterLast("/")
                        )
                        output.parentFile.mkdirs()
                        zipFile.getInputStream(entry).use { input ->
                            output.outputStream().use { outputStream ->
                                input.copyTo(outputStream)
                            }
                        }
                    }
                }
            }
        }

        for (abiName in abis) {
            check(destination.resolve(abiName + "/libgdx.so").isFile) {
                "Missing LibGDX native library for ABI: " + abiName
            }
        }
    }
}
android.sourceSets.getByName("main").jniLibs.srcDir(
    layout.buildDirectory.dir("generated/gdxNatives")
)

tasks.named("preBuild").configure {
    dependsOn(extractGdxNatives)
}
