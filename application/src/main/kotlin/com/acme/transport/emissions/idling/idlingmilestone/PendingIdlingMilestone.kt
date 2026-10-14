package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.text.normalize

// The pending idling milestone records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a pending idling milestone is in its life cycle. */
enum class PendingIdlingMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending idling milestone, with what the planners need to act on it. */
data class PendingIdlingMilestone(
    var code: String,
    var title: String,
    var country: String,
    var depot: String,
    var floorValue: Double,
    var status: PendingIdlingMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingMilestoneStatus): Boolean {
        when (status) {
            PendingIdlingMilestoneStatus.REQUESTED -> return next == PendingIdlingMilestoneStatus.REVOKED || next == PendingIdlingMilestoneStatus.EXPIRED
            PendingIdlingMilestoneStatus.GRANTED -> {
                when (next) {
                    PendingIdlingMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            PendingIdlingMilestoneStatus.REVOKED -> return next == PendingIdlingMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingIdlingMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending idling milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = floorValue

    fun snapshot(): PendingIdlingMilestone {
        return PendingIdlingMilestone(
            code = code,
            title = title,
            country = country,
            depot = depot,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending idling milestone in status requested. */
fun newPendingIdlingMilestone(code: String, title: String): PendingIdlingMilestone {
    return PendingIdlingMilestone(
        code = code,
        title = title,
        country = "",
        depot = "",
        floorValue = 0.0,
        status = PendingIdlingMilestoneStatus.REQUESTED,
    )
}
