package io.violabs.plugins.local.publishing.mavengenerated.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class ManualMavenArtifactsExtension @Inject constructor(private val objectFactory: ObjectFactory) {
    var publicationName: String = "maven"
    var withDokka: Boolean = true
    var name: String? = null
    var description: String? = null
    var websiteUrl: String? = null
    private var licenses: List<License>? = null
    private var developers: List<Developer>? = null
    private var scm: Scm? = null

    fun licenses(block: Licenses.() -> Unit) {
        this.licenses = Licenses().apply(block).licenses()
    }

    fun licenses(): List<License>? {
        return licenses
    }

    fun developers(block: Developers.() -> Unit) {
        this.developers = Developers().apply(block).developers()
    }

    fun developers(): List<Developer>? {
        return developers
    }

    fun scm(block: Scm.() -> Unit) {
        this.scm = Scm(objectFactory).apply(block)
    }

    fun scm(): Scm? {
        return scm
    }

    class License {
        var name: String? = null
        var url: String? = null
    }

    class Licenses {
        private val licenses: MutableList<License> = mutableListOf()

        fun license(block: License.() -> Unit) {
            val license = License().apply(block)
            licenses.add(license)
        }

        fun licenses(): List<License> {
            return licenses
        }
    }

    class Developer {
        var id: String? = null
        var name: String? = null
        var email: String? = null
        var organization: String? = null
    }

    class Developers {
        private val developers: MutableList<Developer> = mutableListOf()

        fun developer(block: Developer.() -> Unit) {
            val developer = Developer().apply(block)
            developers.add(developer)
        }

        fun developers(): List<Developer> {
            return developers
        }
    }

    class Scm(objectFactory: ObjectFactory) {
        val connection: Property<String?> = objectFactory.property(String::class.java).convention(null)
        var developerConnection: String? = null
        var url: String? = null
    }
}