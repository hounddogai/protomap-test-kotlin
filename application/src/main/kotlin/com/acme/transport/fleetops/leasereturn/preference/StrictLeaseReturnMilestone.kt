package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict lease return milestone is in its life cycle. */
enum class StrictLeaseReturnMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict lease return milestone, with what the planners need to act on it. */
data class StrictLeaseReturnMilestone(
    var code: String,
    var label: String,
    var weight: Double,
    var version: Int,
    var segment: String,
    var maximum: Double,
    var status: StrictLeaseReturnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLeaseReturnMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLeaseReturnMilestoneStatus): Boolean {
        when (status) {
            StrictLeaseReturnMilestoneStatus.OPEN -> {
                when (next) {
                    StrictLeaseReturnMilestoneStatus.RESOLVED -> return true
                }
                return false
            }
            StrictLeaseReturnMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    StrictLeaseReturnMilestoneStatus.RESOLVED, StrictLeaseReturnMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            StrictLeaseReturnMilestoneStatus.RESOLVED -> return next == StrictLeaseReturnMilestoneStatus.ARCHIVED
            StrictLeaseReturnMilestoneStatus.REOPENED -> {
                when (next) {
                    StrictLeaseReturnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictLeaseReturnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict lease return milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((weight + maximum) * 100) / 100

    fun snapshot(): StrictLeaseReturnMilestone {
        return StrictLeaseReturnMilestone(
            code = code,
            label = label,
            weight = weight,
            version = version,
            segment = segment,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict lease return milestone in status open. */
fun newStrictLeaseReturnMilestone(code: String, label: String): StrictLeaseReturnMilestone {
    return StrictLeaseReturnMilestone(
        code = code,
        label = label,
        weight = 0.0,
        version = 0,
        segment = "",
        maximum = 0.0,
        status = StrictLeaseReturnMilestoneStatus.OPEN,
    )
}
