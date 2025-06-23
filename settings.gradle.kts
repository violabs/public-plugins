rootProject.name = "public-plugins"

pluginManagement {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com/plugins")
        }
        gradlePluginPortal()
    }
}

include(
    "example",
    "publishing",
    "publishing:digital-ocean",
    "publishing:maven-generated-artifacts"
)