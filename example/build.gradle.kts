import io.violabs.plugins.open.secrets.getPropertyOrEnv

plugins {
    id("io.violabs.plugins.open.publishing.digital-ocean-spaces")
    id("io.violabs.plugins.open.secrets.loader")
}

secretsLoader {
    // Specify the secret file that contains the sensitive information
    secretFile = "super-secret.properties"
    // Or you can use system properties to load secrets
    systemProperties {
        addProperty("apiKey", "API_KEY")
    }
}

digitalOceanSpacesPublishing {
    bucket = "my-example-bucket"
    endpoint = "nyc10.digitaloceanspaces.com" // override default
    region = "nyc10" // override default
    artifactPath = "here-is-my-jar"
    // from secrets loader plugin
    accessKey = project.getPropertyOrEnv("API_KEY")
    secretKey = project.getPropertyOrEnv("API_SECRET")
    dryRun = true // Set to true for dry run mode, no files will be uploaded
}