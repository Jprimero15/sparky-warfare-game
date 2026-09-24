plugins {
    kotlin("jvm")
}

val gdxVersion: String by project

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
}

sourceSets {
    main {
        resources.srcDirs("../assets")
    }
}
