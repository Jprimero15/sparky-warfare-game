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

        configurations.named("gdxNatives").get().files.forEach { jar ->
            java.util.zip.ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".so") }
                    .forEach { entry ->
                        val abi = abis.firstOrNull { entry.name.contains("/" + it + "/") }
                            ?: abis.firstOrNull { entry.name.contains(it + "/") }
                        if (abi != null) {
                            val output = destination.resolve(abi + "/" + entry.name.substringAfterLast('/'))
                            output.parentFile.mkdirs()
                            zip.getInputStream(entry).use { input ->
                                output.outputStream().use { outputStream -> input.copyTo(outputStream) }
                            }
                        }
                    }
            }
        }

        check(abis.all { destination.resolve(it + "/libgdx.so").isFile }) {
            "LibGDX native libraries were not extracted correctly"
        }
    }
}

android.sourceSets.getByName("main").jniLibs.srcDir(
    layout.buildDirectory.dir("generated/gdxNatives")
)

tasks.named("preBuild").configure {
    dependsOn(extractGdxNatives)
}
