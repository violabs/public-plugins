package io.violabs.test.core

abstract class UnitTest {
    protected fun <A> testEquals(expected: A, actual: A): Boolean {
        if (expected == actual) return true

        println("EXPECT: $expected\nACTUAL: $actual")
        return expected == actual
    }
}