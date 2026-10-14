package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled route offset snapshot is in its life cycle. */
enum class ScheduledRouteOffsetSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled route offset snapshot as the route offset team keeps it. */
data class ScheduledRouteOffsetSnapshot(
    var code: String,
    var label: String,
    var windowDays: Int,
    var name: String,
    var minimum: Double,
    var version: Int,
    var active: Boolean,
    var stopCount: Int,
    var status: ScheduledRouteOffsetSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetSnapshotStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetSnapshotStatus.PROPOSED -> return next == ScheduledRouteOffsetSnapshotStatus.APPROVED
            ScheduledRouteOffsetSnapshotStatus.APPROVED -> {
                when (next) {
                    ScheduledRouteOffsetSnapshotStatus.REJECTED, ScheduledRouteOffsetSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledRouteOffsetSnapshotStatus.REJECTED -> return next == ScheduledRouteOffsetSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset snapshot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round(version.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledRouteOffsetSnapshot {
        return ScheduledRouteOffsetSnapshot(
            code = code,
            label = label,
            windowDays = windowDays,
            name = name,
            minimum = minimum,
            version = version,
            active = active,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled route offset snapshot in status proposed. */
fun newScheduledRouteOffsetSnapshot(code: String, label: String): ScheduledRouteOffsetSnapshot {
    return ScheduledRouteOffsetSnapshot(
        code = code,
        label = label,
        windowDays = 0,
        name = "",
        minimum = 0.0,
        version = 0,
        active = false,
        stopCount = 0,
        status = ScheduledRouteOffsetSnapshotStatus.PROPOSED,
    )
}
