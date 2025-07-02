rootProject.name = "public-plugins"

pluginManagement {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com/plugins")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    "example",
    "publishing:digital-ocean-spaces",
    "publishing:digital-ocean-spaces-core",
    "publishing:maven-generated-artifacts",
    "publishing:maven-generated-artifacts-core",
    "local:publishing:digital-ocean-spaces",
    "local:publishing:maven-generated-artifacts",
)