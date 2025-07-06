package io.violabs.plugins.open.publishing.digitalocean.domain

/**
 * The collection of information that identifies an artifact in a DigitalOcean Spaces bucket.
 * @property groupId the group ID of the artifact. `.` separated.
 * @property artifactId the artifact ID of the artifact. `-` separated.
 * @property version the version of the artifact. `.` separated.
 */
data class Namespace(
    val groupId: String,
    val artifactId: String,
    val version: String
) {
    val artifactVersion: String = "$artifactId-$version"
    val groupPath: String = groupId.replace('.', '/')
    val artifactPath: String = "$groupPath/$artifactId"
    val artifactFullPath: String = "$groupPath/$artifactId/$version"
    val pluginFullPath: String = "$artifactPath/$groupId.$artifactId.gradle.plugin/$version"

    val jarName: String = "$artifactVersion.jar"
    val jarSha1Name: String = "$jarName.sha1"
    val jarSha256Name: String = "$jarName.sha256"
    val jarPath: String = "libs/$jarName"
    val jarSha1Path: String = "libs/$jarSha1Name"
    val jarSha256Path: String = "libs/$jarSha256Name"
    val jarKey: String = "$artifactFullPath/$jarName"
    val jarSha1Key: String = "$artifactFullPath/$jarSha1Name"
    val jarSha256Key: String = "$artifactFullPath/$jarSha256Name"

    val sourcesJarName: String = "$artifactVersion-sources.jar"
    val sourcesJarSha1Name: String = "$sourcesJarName.sha1"
    val sourcesJarSha256Name: String = "$sourcesJarName.sha256"
    val sourcesJarPath: String = "libs/$sourcesJarName"
    val sourcesJarSha1Path: String = "libs/$sourcesJarSha1Name"
    val sourcesJarSha256Path: String = "libs/$sourcesJarSha256Name"
    val sourcesJarKey: String = "$artifactFullPath/$sourcesJarName"
    val sourcesJarSha1Key: String = "$artifactFullPath/$sourcesJarSha1Name"
    val sourcesJarSha256Key: String = "$artifactFullPath/$sourcesJarSha256Name"

    val javadocJarName: String = "$artifactVersion-javadoc.jar"
    val javadocJarSha1Name: String = "$javadocJarName.sha1"
    val javadocJarSha256Name: String = "$javadocJarName.sha256"
    val javadocJarPath: String = "libs/$javadocJarName"
    val javadocJarSha1Path: String = "libs/$javadocJarSha1Name"
    val javadocJarSha256Path: String = "libs/$javadocJarSha256Name"
    val javadocJarKey: String = "$artifactFullPath/$javadocJarName"
    val javadocJarSha1Key: String = "$artifactFullPath/$javadocJarSha1Name"
    val javadocJarSha256Key: String = "$artifactFullPath/$javadocJarSha256Name"

    val kdocJarName: String = "$artifactVersion-kdoc.jar"
    val kdocJarSha1Name: String = "$kdocJarName.sha1"
    val kdocJarSha256Name: String = "$kdocJarName.sha256"
    val kdocJarPath: String = "libs/$kdocJarName"
    val kdocJarSha1Path: String = "libs/$kdocJarSha1Name"
    val kdocJarSha256Path: String = "libs/$kdocJarSha256Name"
    val kdocJarKey: String = "$artifactFullPath/$kdocJarName"
    val kdocJarSha1Key: String = "$artifactFullPath/$kdocJarSha1Name"
    val kdocJarSha256Key: String = "$artifactFullPath/$kdocJarSha256Name"

    val sourcePomName: String = "publications/digitalOceanSpaces/pom-default.xml"
    val pomName: String = "$artifactVersion.pom"
    val pomSha1Name: String = "$pomName.sha1"
    val pomSha256Name: String = "$pomName.sha256"
    val pomPath: String = "libs/$pomName"
    val pomSha1Path: String = "libs/$pomSha1Name"
    val pomSha256Path: String = "libs/$pomSha256Name"
    val pomKey: String = "$artifactFullPath/$pomName"
    val pomSha1Key: String = "$artifactFullPath/$pomSha1Name"
    val pomSha256Key: String = "$artifactFullPath/$pomSha256Name"

    val pluginPomName: String = "$groupId.$artifactId.gradle.plugin.$version.pom"
    val pluginPomSha1Name: String = "$pluginPomName.sha1"
    val pluginPomSha256Name: String = "$pluginPomName.sha256"
    val pluginPomPath: String = "libs/$pluginPomName"
    val pluginPomSha1Path: String = "libs/$pluginPomSha1Name"
    val pluginPomSha256Path: String = "libs/$pluginPomSha256Name"
    val pluginPomKey: String = "$pluginFullPath/$pluginPomName"
    val pluginPomSha1Key: String = "$pluginFullPath/$pluginPomSha1Name"
    val pluginPomSha256Key: String = "$pluginFullPath/$pluginPomSha256Name"
}