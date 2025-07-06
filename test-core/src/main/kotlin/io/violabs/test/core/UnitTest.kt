package io.violabs.test.core

abstract class UnitTest {
    protected fun <A> testEquals(expected: A, actual: A): Boolean {
        if (expected == actual) return true

        println("EXPECT: $expected\nACTUAL: $actual")
        return expected == actual
    }

    protected infix fun <T> T.eq(expected: T) {
        if (this == expected) return

        assert(this == expected) {
            "ACTUAL: $this\nEXPECT: $expected"
        }
    }
}