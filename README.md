```kotlin
abstract class DigitalOceanSpacesExtension {
    abstract val endpoint: Property<String>
    abstract val bucket: Property<String>
    abstract val region: Property<String>
    abstract val accessKey: Property<String>
    abstract val secretKey: Property<String>
    abstract val artifactPath: Property<String>
    abstract val jarQualifier: Property<String>
    abstract val dryRun: Property<Boolean>
    
    // New properties for exception handling
    abstract val continueOnVersionCheckFailure: Property<Boolean>
    abstract val suppressDetailedExceptions: Property<Boolean>

    init {
        // Set defaults
        dryRun.convention(false)
        continueOnVersionCheckFailure.convention(false)
        suppressDetailedExceptions.convention(true) // Default to suppressing detailed exceptions
    }
}
```