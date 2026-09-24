plugins {
    kotlin("jvm")
}

val gdxVersion: String by project

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
}

sourceSets {
    main {
        resources.srcDirs("../assets")
    }
}
