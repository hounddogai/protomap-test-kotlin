package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset milestone is in its life cycle. */
enum class FallbackRouteOffsetMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback route offset milestone and its current status. */
data class FallbackRouteOffsetMilestone(
    var code: String,
    var title: String,
    var floorValue: Double,
    var retries: Int,
    var depot: String,
    var pallets: Int,
    var status: FallbackRouteOffsetMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetMilestoneStatus): Boolean {
        when (status) {
            FallbackRouteOffsetMilestoneStatus.REQUESTED -> {
                when (next) {
                    FallbackRouteOffsetMilestoneStatus.GRANTED, FallbackRouteOffsetMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            FallbackRouteOffsetMilestoneStatus.GRANTED -> return next == FallbackRouteOffsetMilestoneStatus.REVOKED
            FallbackRouteOffsetMilestoneStatus.REVOKED -> return next == FallbackRouteOffsetMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((floorValue - retries.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetMilestone {
        return FallbackRouteOffsetMilestone(
            code = code,
            title = title,
            floorValue = floorValue,
            retries = retries,
            depot = depot,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback route offset milestone in status requested. */
fun newFallbackRouteOffsetMilestone(code: String, title: String): FallbackRouteOffsetMilestone {
    return FallbackRouteOffsetMilestone(
        code = code,
        title = title,
        floorValue = 0.0,
        retries = 0,
        depot = "",
        pallets = 0,
        status = FallbackRouteOffsetMilestoneStatus.REQUESTED,
    )
}
