

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.violabs.plugins.local.secrets.loader")
}

group = "io.violabs.public-plugins"
version = "0.0.1"

repositories {
    // Add any required repositories
    mavenCentral()
}