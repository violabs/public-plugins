package io.violabs.plugins.open.publishing.mavengenerated

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

abstract class UnitTest {
    protected val logger: Logger = Logging.getLogger(UnitTest::class.java)

    protected fun <A> testEquals(expected: A, actual: A): Boolean {
        if (expected == actual) return true

        println("EXPECT: $expected\nACTUAL: $actual")
        return expected == actual
    }
}