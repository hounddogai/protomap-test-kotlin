package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize

/** Where a utilization milestone is in its life cycle. */
enum class UtilizationMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One utilization milestone, with what the planners need to act on it. */
data class UtilizationMilestone(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var segment: String,
    var contactEmail: String,
    var amount: Double,
    var status: UtilizationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationMilestoneStatus): Boolean {
        when (status) {
            UtilizationMilestoneStatus.PLANNED -> return next == UtilizationMilestoneStatus.SCHEDULED
            UtilizationMilestoneStatus.SCHEDULED -> {
                when (next) {
                    UtilizationMilestoneStatus.IN_PROGRESS, UtilizationMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationMilestoneStatus.IN_PROGRESS -> return next == UtilizationMilestoneStatus.COMPLETED || next == UtilizationMilestoneStatus.CANCELLED
            UtilizationMilestoneStatus.COMPLETED -> {
                when (next) {
                    UtilizationMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("utilization milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): UtilizationMilestone {
        return UtilizationMilestone(
            code = code,
            label = label,
            pinned = pinned,
            segment = segment,
            contactEmail = contactEmail,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a utilization milestone in status planned. */
fun newUtilizationMilestone(code: String, label: String): UtilizationMilestone {
    return UtilizationMilestone(
        code = code,
        label = label,
        pinned = false,
        segment = "",
        contactEmail = "",
        amount = 0.0,
        status = UtilizationMilestoneStatus.PLANNED,
    )
}
