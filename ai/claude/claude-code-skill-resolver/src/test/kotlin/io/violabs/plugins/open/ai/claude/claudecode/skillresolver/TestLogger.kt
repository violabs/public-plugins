package io.violabs.plugins.open.ai.claude.claudecode.skillresolver

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.slf4j.Marker

/**
 * Simple test logger that prints to stdout.
 * Implements all required Logger methods.
 */
class TestLogger : Logger {
    override fun getName() = "TestLogger"

    // Trace
    override fun isTraceEnabled() = false
    override fun isTraceEnabled(marker: Marker?) = false
    override fun trace(message: String?) {}
    override fun trace(format: String?, arg: Any?) {}
    override fun trace(format: String?, arg1: Any?, arg2: Any?) {}
    override fun trace(format: String?, vararg arguments: Any?) {}
    override fun trace(message: String?, throwable: Throwable?) {}
    override fun trace(marker: Marker?, message: String?) {}
    override fun trace(marker: Marker?, format: String?, arg: Any?) {}
    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}
    override fun trace(marker: Marker?, format: String?, vararg arguments: Any?) {}
    override fun trace(marker: Marker?, message: String?, throwable: Throwable?) {}

    // Debug
    override fun isDebugEnabled() = true
    override fun isDebugEnabled(marker: Marker?) = true
    override fun debug(message: String?) { println("[DEBUG] $message") }
    override fun debug(format: String?, arg: Any?) { println("[DEBUG] $format") }
    override fun debug(format: String?, arg1: Any?, arg2: Any?) { println("[DEBUG] $format") }
    override fun debug(format: String?, vararg arguments: Any?) { println("[DEBUG] $format") }
    override fun debug(message: String?, throwable: Throwable?) { println("[DEBUG] $message"); throwable?.printStackTrace() }
    override fun debug(marker: Marker?, message: String?) { println("[DEBUG] $message") }
    override fun debug(marker: Marker?, format: String?, arg: Any?) { println("[DEBUG] $format") }
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) { println("[DEBUG] $format") }
    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) { println("[DEBUG] $format") }
    override fun debug(marker: Marker?, message: String?, throwable: Throwable?) { println("[DEBUG] $message"); throwable?.printStackTrace() }

    // Info
    override fun isInfoEnabled() = true
    override fun isInfoEnabled(marker: Marker?) = true
    override fun info(message: String?) { println("[INFO] $message") }
    override fun info(format: String?, arg: Any?) { println("[INFO] $format") }
    override fun info(format: String?, arg1: Any?, arg2: Any?) { println("[INFO] $format") }
    override fun info(format: String?, vararg arguments: Any?) { println("[INFO] $format") }
    override fun info(message: String?, throwable: Throwable?) { println("[INFO] $message"); throwable?.printStackTrace() }
    override fun info(marker: Marker?, message: String?) { println("[INFO] $message") }
    override fun info(marker: Marker?, format: String?, arg: Any?) { println("[INFO] $format") }
    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) { println("[INFO] $format") }
    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) { println("[INFO] $format") }
    override fun info(marker: Marker?, message: String?, throwable: Throwable?) { println("[INFO] $message"); throwable?.printStackTrace() }

    // Warn
    override fun isWarnEnabled() = true
    override fun isWarnEnabled(marker: Marker?) = true
    override fun warn(message: String?) { println("[WARN] $message") }
    override fun warn(format: String?, arg: Any?) { println("[WARN] $format") }
    override fun warn(format: String?, arg1: Any?, arg2: Any?) { println("[WARN] $format") }
    override fun warn(format: String?, vararg arguments: Any?) { println("[WARN] $format") }
    override fun warn(message: String?, throwable: Throwable?) { println("[WARN] $message"); throwable?.printStackTrace() }
    override fun warn(marker: Marker?, message: String?) { println("[WARN] $message") }
    override fun warn(marker: Marker?, format: String?, arg: Any?) { println("[WARN] $format") }
    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) { println("[WARN] $format") }
    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) { println("[WARN] $format") }
    override fun warn(marker: Marker?, message: String?, throwable: Throwable?) { println("[WARN] $message"); throwable?.printStackTrace() }

    // Error
    override fun isErrorEnabled() = true
    override fun isErrorEnabled(marker: Marker?) = true
    override fun error(message: String?) { println("[ERROR] $message") }
    override fun error(format: String?, arg: Any?) { println("[ERROR] $format") }
    override fun error(format: String?, arg1: Any?, arg2: Any?) { println("[ERROR] $format") }
    override fun error(format: String?, vararg arguments: Any?) { println("[ERROR] $format") }
    override fun error(message: String?, throwable: Throwable?) { println("[ERROR] $message"); throwable?.printStackTrace() }
    override fun error(marker: Marker?, message: String?) { println("[ERROR] $message") }
    override fun error(marker: Marker?, format: String?, arg: Any?) { println("[ERROR] $format") }
    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) { println("[ERROR] $format") }
    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) { println("[ERROR] $format") }
    override fun error(marker: Marker?, message: String?, throwable: Throwable?) { println("[ERROR] $message"); throwable?.printStackTrace() }

    // Gradle-specific
    override fun isLifecycleEnabled() = true
    override fun lifecycle(message: String?) { println("[LIFECYCLE] $message") }
    override fun lifecycle(message: String?, vararg objects: Any?) { println("[LIFECYCLE] $message") }
    override fun lifecycle(message: String?, throwable: Throwable?) { println("[LIFECYCLE] $message"); throwable?.printStackTrace() }

    override fun isQuietEnabled() = true
    override fun quiet(message: String?) { println("[QUIET] $message") }
    override fun quiet(message: String?, vararg objects: Any?) { println("[QUIET] $message") }
    override fun quiet(message: String?, throwable: Throwable?) { println("[QUIET] $message"); throwable?.printStackTrace() }

    override fun isEnabled(level: LogLevel?) = true
    override fun log(level: LogLevel?, message: String?) { println("[$level] $message") }
    override fun log(level: LogLevel?, message: String?, vararg objects: Any?) { println("[$level] $message") }
    override fun log(level: LogLevel?, message: String?, throwable: Throwable?) { println("[$level] $message") }
}
