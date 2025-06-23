package io.violabs.plugins.local.secrets

object Ext {
    @JvmInline
    value class Key(val value: String)

    @JvmInline
    value class SysPropName(val value: String)
}
