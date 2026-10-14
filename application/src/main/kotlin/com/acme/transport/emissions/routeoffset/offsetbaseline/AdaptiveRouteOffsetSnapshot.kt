package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive route offset snapshot is in its life cycle. */
enum class AdaptiveRouteOffsetSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive route offset snapshot as the route offset team keeps it. */
data class AdaptiveRouteOffsetSnapshot(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var retries: Int,
    var vehicleId: String,
    var status: AdaptiveRouteOffsetSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRouteOffsetSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRouteOffsetSnapshotStatus): Boolean {
        when (status) {
            AdaptiveRouteOffsetSnapshotStatus.PROPOSED -> {
                when (next) {
                    AdaptiveRouteOffsetSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetSnapshotStatus.APPROVED -> {
                when (next) {
                    AdaptiveRouteOffsetSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetSnapshotStatus.REJECTED -> return next == AdaptiveRouteOffsetSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveRouteOffsetSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive route offset snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive route offset snapshot"} $code"

    fun effectiveValue(): Double = round(expiresAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): AdaptiveRouteOffsetSnapshot {
        return AdaptiveRouteOffsetSnapshot(
            code = code,
            name = name,
            expiresAt = expiresAt,
            retries = retries,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive route offset snapshot in status proposed. */
fun newAdaptiveRouteOffsetSnapshot(code: String, name: String): AdaptiveRouteOffsetSnapshot {
    return AdaptiveRouteOffsetSnapshot(
        code = code,
        name = name,
        expiresAt = 0,
        retries = 0,
        vehicleId = "",
        status = AdaptiveRouteOffsetSnapshotStatus.PROPOSED,
    )
}
