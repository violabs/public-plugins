
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