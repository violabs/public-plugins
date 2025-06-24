import io.violabs.plugins.local.secrets.getPropertyOrEnv

plugins {
    `kotlin-dsl`
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com/plugins")
        }
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    // Add any required repositories
    maven {
        name = "DigitalOceanSpacesCDN"
        url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com/plugins")
    }
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.25.27")
    implementation("io.violabs.plugins.open.publishing:maven-generated-artifacts:0.0.2")
}

gradlePlugin {
    plugins {
        create("digitalOceanSpacesPlugin") {
            id = "io.violabs.plugins.open.publishing.digital-ocean-spaces"
            version = "0.0.1"
            implementationClass = "io.violabs.plugins.open.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }
}


digitalOceanSpacesPublishing {
    artifactPath = "io/violabs/plugins/open/publishing/digital-ocean-spaces/$version"
}

mavenGeneratedArtifacts {
    name = "Digital Ocean Spaces Publishing"
    description = """
            This plugin publishes the build jar, sources jar, pom, and optionally dokka jars.
        """
    websiteUrl = "https://github.com/violabs/public-plugins/tree/main/publishing/digital-ocean-spaces"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "violabs"
            name = "Violabs Team"
            email = "support@violabs.io"
            organization = "Violabs Software"
        }
    }

    scm {
        connection = "https://github.com/violabs/public-plugins.git"
    }
}