package io.violabs.plugins.local.publishing.projectsync

import java.io.File

open class ProjectSyncExtension {
    var syncSource: File? = null
    var syncTarget: File? = null
    private var autoSync: Boolean = false
    private var deviation: Deviation? = null

    fun autoSync(enabled: Boolean = true) {
        this.autoSync = enabled
    }

    fun isAutoSyncEnabled(): Boolean {
        return autoSync
    }

    fun deviation(block: Deviation.() -> Unit) {
        val deviation = Deviation()
        block(deviation)
        this.deviation = deviation
    }

    fun deviation(): Deviation? {
        return deviation
    }

    open class Deviation {
        var reason: String = ""
    }
}