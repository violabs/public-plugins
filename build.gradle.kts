

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.violabs.plugins.local.secrets.loader")
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

group = "io.violabs.public-plugins"
version = "0.0.1"

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("org.jetbrains.dokka")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}