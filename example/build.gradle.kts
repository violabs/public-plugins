plugins {
//    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.local.secrets.loader")
    id("org.jetbrains.dokka") version "1.9.20"
}

secretsLoader {
    // Specify the secret file that contains the sensitive information
    secretFile = "super-secret.properties"
    // Or you can use system properties to load secrets
    systemProperties {
        addProperty("apiKey", "API_KEY")
    }
}

mavenGeneratedArtifacts {
    scm {
        connection = "github.com/violabs/${project.name}.git"
    }
}

//digitalOceanSpacesPublishing {
//    bucket = "my-example-bucket"
//    endpoint = "nyc10.digitaloceanspaces.com" // override default
//    region = "nyc10" // override default
//    artifactPath = "here-is-my-jar"
//    // from secrets loader plugin
//    accessKey = project.getPropertyOrEnv("API_KEY")
//    secretKey = project.getPropertyOrEnv("API_SECRET")
//    dryRun = true // Set to true for dry run mode, no files will be uploaded
//}