package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending camera event bundle records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a pending camera event bundle is in its life cycle. */
enum class PendingCameraEventBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending camera event bundle. Its code is unique within a tenant. */
data class PendingCameraEventBundle(
    var code: String,
    var title: String,
    var routeCode: String,
    var revision: Int,
    var graceDays: Int,
    var source: String,
    var version: Int,
    var status: PendingCameraEventBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCameraEventBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCameraEventBundleStatus): Boolean {
        when (status) {
            PendingCameraEventBundleStatus.IDLE -> return next == PendingCameraEventBundleStatus.READY || next == PendingCameraEventBundleStatus.DRAINING
            PendingCameraEventBundleStatus.WARMING -> return next == PendingCameraEventBundleStatus.READY || next == PendingCameraEventBundleStatus.DRAINING
            PendingCameraEventBundleStatus.READY -> {
                when (next) {
                    PendingCameraEventBundleStatus.DRAINING -> return true
                }
                return false
            }
            PendingCameraEventBundleStatus.DRAINING -> return next == PendingCameraEventBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingCameraEventBundleStatus) {
        if (!canMoveTo(next)) {
            error("pending camera event bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((graceDays.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): PendingCameraEventBundle {
        return PendingCameraEventBundle(
            code = code,
            title = title,
            routeCode = routeCode,
            revision = revision,
            graceDays = graceDays,
            source = source,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending camera event bundle in status idle. */
fun newPendingCameraEventBundle(code: String, title: String): PendingCameraEventBundle {
    return PendingCameraEventBundle(
        code = code,
        title = title,
        routeCode = "",
        revision = 0,
        graceDays = 0,
        source = "",
        version = 0,
        status = PendingCameraEventBundleStatus.IDLE,
    )
}
