plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.25.27")
}

gradlePlugin {
    plugins {
        create("secretsLoaderPlugin") {
            id = "io.violabs.plugins.local.secrets.loader"
            version = "0.0.1"
            implementationClass = "io.violabs.plugins.local.secrets.SecretsLoaderPlugin"
        }
    }

    plugins {
        create("localDigitalOceanSpacesPlugin") {
            id = "io.violabs.plugins.local.publishing.digital-ocean-spaces"
            version = "0.0.1"
            implementationClass = "io.violabs.plugins.local.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }

    plugins {
        create("localMavenGeneratedArtifacts") {
            id = "io.violabs.plugins.local.publishing.maven-generated-artifacts"
            version = "0.0.1"
            implementationClass = "io.violabs.plugins.local.publishing.ManualMavenArtifactsPlugin"
        }
    }
}