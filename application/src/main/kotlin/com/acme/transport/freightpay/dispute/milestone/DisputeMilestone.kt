package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.text.normalize

// The dispute milestone records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute milestone is in its life cycle. */
enum class DisputeMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a dispute milestone and its current status. */
data class DisputeMilestone(
    var code: String,
    var label: String,
    var lane: String,
    var archived: Boolean,
    var updatedAt: Int,
    var status: DisputeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeMilestoneStatus): Boolean {
        when (status) {
            DisputeMilestoneStatus.OPEN -> {
                when (next) {
                    DisputeMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            DisputeMilestoneStatus.IN_REVIEW -> return next == DisputeMilestoneStatus.ARCHIVED
            DisputeMilestoneStatus.RESOLVED -> return next == DisputeMilestoneStatus.REOPENED
            DisputeMilestoneStatus.REOPENED -> return next == DisputeMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DisputeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("dispute milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): DisputeMilestone {
        return DisputeMilestone(
            code = code,
            label = label,
            lane = lane,
            archived = archived,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dispute milestone in status open. */
fun newDisputeMilestone(code: String, label: String): DisputeMilestone {
    return DisputeMilestone(
        code = code,
        label = label,
        lane = "",
        archived = false,
        updatedAt = 0,
        status = DisputeMilestoneStatus.OPEN,
    )
}
