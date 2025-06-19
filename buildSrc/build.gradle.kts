plugins {
    `kotlin-dsl`
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
        create("digitalOceanSpacesPlugin") {
            id = "io.violabs.plugins.open.publishing.digital-ocean-spaces"
            implementationClass = "io.violabs.plugins.open.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }
}