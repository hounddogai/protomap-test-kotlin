package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.text.normalize

// The cross dock manifest records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock manifest is in its life cycle. */
enum class CrossDockManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cross dock manifest. Its code is unique within a tenant. */
data class CrossDockManifest(
    var code: String,
    var title: String,
    var routeCode: String,
    var capacity: Int,
    var depot: String,
    var status: CrossDockManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockManifestStatus): Boolean {
        when (status) {
            CrossDockManifestStatus.NEW -> return next == CrossDockManifestStatus.RUNNING || next == CrossDockManifestStatus.DONE
            CrossDockManifestStatus.QUEUED -> {
                when (next) {
                    CrossDockManifestStatus.RUNNING, CrossDockManifestStatus.DONE -> return true
                }
                return false
            }
            CrossDockManifestStatus.RUNNING -> return next == CrossDockManifestStatus.DONE || next == CrossDockManifestStatus.FAILED
            CrossDockManifestStatus.DONE -> {
                when (next) {
                    CrossDockManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockManifestStatus) {
        if (!canMoveTo(next)) {
            error("cross dock manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cross dock manifest"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): CrossDockManifest {
        return CrossDockManifest(
            code = code,
            title = title,
            routeCode = routeCode,
            capacity = capacity,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a cross dock manifest in status new. */
fun newCrossDockManifest(code: String, title: String): CrossDockManifest {
    return CrossDockManifest(
        code = code,
        title = title,
        routeCode = "",
        capacity = 0,
        depot = "",
        status = CrossDockManifestStatus.NEW,
    )
}
