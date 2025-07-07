

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.violabs.plugins.local.secrets.gradle-loader")
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

group = "io.violabs.public-plugins"
version = "0.0.1"

repositories {
    mavenCentral()
}

extra["publishingDigitalOceanSpacesVersion"] = "0.0.9"
extra["publishingMavenGeneratedArtifactsVersion"] = "0.0.13"
extra["secretsGradleLoaderVersion"] = "0.0.3"

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
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("io.mockk:mockk:1.13.8")
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