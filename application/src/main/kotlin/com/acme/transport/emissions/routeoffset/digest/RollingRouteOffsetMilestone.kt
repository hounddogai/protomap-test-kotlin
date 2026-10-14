package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling route offset milestone is in its life cycle. */
enum class RollingRouteOffsetMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling route offset milestone, with what the planners need to act on it. */
data class RollingRouteOffsetMilestone(
    var code: String,
    var label: String,
    var approved: Boolean,
    var stopCount: Int,
    var summary: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var vehicleId: String,
    var status: RollingRouteOffsetMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRouteOffsetMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRouteOffsetMilestoneStatus): Boolean {
        when (status) {
            RollingRouteOffsetMilestoneStatus.PLANNED -> return next == RollingRouteOffsetMilestoneStatus.IN_PROGRESS || next == RollingRouteOffsetMilestoneStatus.CANCELLED
            RollingRouteOffsetMilestoneStatus.SCHEDULED -> return next == RollingRouteOffsetMilestoneStatus.IN_PROGRESS || next == RollingRouteOffsetMilestoneStatus.COMPLETED
            RollingRouteOffsetMilestoneStatus.IN_PROGRESS -> return next == RollingRouteOffsetMilestoneStatus.COMPLETED || next == RollingRouteOffsetMilestoneStatus.CANCELLED
            RollingRouteOffsetMilestoneStatus.COMPLETED -> {
                when (next) {
                    RollingRouteOffsetMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRouteOffsetMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling route offset milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((durationMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): RollingRouteOffsetMilestone {
        return RollingRouteOffsetMilestone(
            code = code,
            label = label,
            approved = approved,
            stopCount = stopCount,
            summary = summary,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling route offset milestone in status planned. */
fun newRollingRouteOffsetMilestone(code: String, label: String): RollingRouteOffsetMilestone {
    return RollingRouteOffsetMilestone(
        code = code,
        label = label,
        approved = false,
        stopCount = 0,
        summary = "",
        durationMinutes = 0,
        distanceKm = 0.0,
        vehicleId = "",
        status = RollingRouteOffsetMilestoneStatus.PLANNED,
    )
}
