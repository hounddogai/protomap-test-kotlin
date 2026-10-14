package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.normalize

/** Where a fallback backhaul milestone is in its life cycle. */
enum class FallbackBackhaulMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback backhaul milestone. Its code is unique within a tenant. */
data class FallbackBackhaulMilestone(
    var code: String,
    var title: String,
    var vehicleId: String,
    var ownerId: String,
    var driverId: String,
    var cost: Double,
    var status: FallbackBackhaulMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulMilestoneStatus): Boolean {
        when (status) {
            FallbackBackhaulMilestoneStatus.IDLE -> {
                when (next) {
                    FallbackBackhaulMilestoneStatus.READY, FallbackBackhaulMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            FallbackBackhaulMilestoneStatus.WARMING -> return next == FallbackBackhaulMilestoneStatus.DRAINING
            FallbackBackhaulMilestoneStatus.READY -> {
                when (next) {
                    FallbackBackhaulMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            FallbackBackhaulMilestoneStatus.DRAINING -> return next == FallbackBackhaulMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback backhaul milestone"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): FallbackBackhaulMilestone {
        return FallbackBackhaulMilestone(
            code = code,
            title = title,
            vehicleId = vehicleId,
            ownerId = ownerId,
            driverId = driverId,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback backhaul milestone in status idle. */
fun newFallbackBackhaulMilestone(code: String, title: String): FallbackBackhaulMilestone {
    return FallbackBackhaulMilestone(
        code = code,
        title = title,
        vehicleId = "",
        ownerId = "",
        driverId = "",
        cost = 0.0,
        status = FallbackBackhaulMilestoneStatus.IDLE,
    )
}
