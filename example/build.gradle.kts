//plugins {
//    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
//    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
//    id("io.violabs.plugins.local.secrets.loader")
//    id("org.jetbrains.dokka") version "1.9.20"
//}
//
//secretsLoader {
//    // Specify the secret file that contains the sensitive information
//    secretFile = "super-secret.properties"
//    // Or you can use system properties to load secrets
//    systemProperties {
//        addProperty("apiKey", "API_KEY")
//    }
//}
//
//mavenGeneratedArtifacts {
//    scm {
//        connection = "github.com/violabs/${project.name}.git"
//    }
//}
//
////digitalOceanSpacesPublishing {
////    bucket = "open-reliquary"
////    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
////    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
////    artifactPath = "plugins/io/violabs/plugins/open/publishing/maven-generated-artifacts/$version"
////    isPlugin = true
////}
