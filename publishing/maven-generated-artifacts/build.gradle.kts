import io.violabs.plugins.local.secrets.getPropertyOrEnv

plugins {
    `kotlin-dsl`
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.violabs.plugins.open.publishing"
version = "0.0.1"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("maven-generated-artifacts")
//    dependsOn(subprojects.map { it.tasks.named("classes") })
//
//    // Pull in each subproject’s compiled classes & resources
//    from(subprojects.map { proj ->
//        proj.extensions.getByType<SourceSetContainer>()["main"].output
//    })
}

repositories {
    // Add any required repositories
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("mavenGeneratedArtifacts") {
            id = "io.violabs.plugins.open.publishing.maven-generated-artifacts"
            version = project.version.toString()
            implementationClass = "io.violabs.plugins.open.publishing.ManualMavenArtifactsPlugin"
        }
    }
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    artifactPath = "plugins/io/violabs/plugins/open/publishing/maven-generated-artifacts/$version"
    isPlugin = true
}