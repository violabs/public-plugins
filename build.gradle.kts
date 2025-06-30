

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
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.junit.jupiter:junit-jupiter-api:5.13.0-M2")
        implementation("io.mockk:mockk:1.13.17")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
}