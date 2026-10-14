package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.text.normalize

/** Where a violation milestone is in its life cycle. */
enum class ViolationMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a violation milestone and its current status. */
data class ViolationMilestone(
    var code: String,
    var label: String,
    var visible: Boolean,
    var channel: String,
    var updatedAt: Int,
    var contactEmail: String,
    var status: ViolationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationMilestoneStatus): Boolean {
        when (status) {
            ViolationMilestoneStatus.CREATED -> return next == ViolationMilestoneStatus.PUBLISHED
            ViolationMilestoneStatus.VALIDATED -> return next == ViolationMilestoneStatus.PUBLISHED || next == ViolationMilestoneStatus.RETIRED
            ViolationMilestoneStatus.PUBLISHED -> return next == ViolationMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ViolationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("violation milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): ViolationMilestone {
        return ViolationMilestone(
            code = code,
            label = label,
            visible = visible,
            channel = channel,
            updatedAt = updatedAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a violation milestone in status created. */
fun newViolationMilestone(code: String, label: String): ViolationMilestone {
    return ViolationMilestone(
        code = code,
        label = label,
        visible = false,
        channel = "",
        updatedAt = 0,
        contactEmail = "",
        status = ViolationMilestoneStatus.CREATED,
    )
}
