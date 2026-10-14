package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize

/** Where a locker override is in its life cycle. */
enum class LockerOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One locker override, with what the planners need to act on it. */
data class LockerOverride(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var lane: String,
    var routeCode: String,
    var driverId: String,
    var summary: String,
    var status: LockerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerOverrideStatus): Boolean {
        when (status) {
            LockerOverrideStatus.PLANNED -> {
                when (next) {
                    LockerOverrideStatus.IN_PROGRESS, LockerOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            LockerOverrideStatus.SCHEDULED -> return next == LockerOverrideStatus.IN_PROGRESS || next == LockerOverrideStatus.CANCELLED
            LockerOverrideStatus.IN_PROGRESS -> return next == LockerOverrideStatus.COMPLETED || next == LockerOverrideStatus.CANCELLED
            LockerOverrideStatus.COMPLETED -> {
                when (next) {
                    LockerOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("locker override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): LockerOverride {
        return LockerOverride(
            code = code,
            label = label,
            distanceKm = distanceKm,
            lane = lane,
            routeCode = routeCode,
            driverId = driverId,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker override in status planned. */
fun newLockerOverride(code: String, label: String): LockerOverride {
    return LockerOverride(
        code = code,
        label = label,
        distanceKm = 0.0,
        lane = "",
        routeCode = "",
        driverId = "",
        summary = "",
        status = LockerOverrideStatus.PLANNED,
    )
}
