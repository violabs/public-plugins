package io.violabs.plugins.open.secrets.gradleloader.domain

object Ext {
    @JvmInline
    value class Key(val value: String)

    @JvmInline
    value class SysPropName(val value: String)
}
