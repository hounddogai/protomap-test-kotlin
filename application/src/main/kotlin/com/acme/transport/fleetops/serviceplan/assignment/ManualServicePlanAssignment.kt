package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize

// The manual service plan assignment records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a manual service plan assignment is in its life cycle. */
enum class ManualServicePlanAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual service plan assignment. Its code is unique within a tenant. */
data class ManualServicePlanAssignment(
    var code: String,
    var title: String,
    var amount: Double,
    var channel: String,
    var trailerId: String,
    var status: ManualServicePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanAssignmentStatus): Boolean {
        when (status) {
            ManualServicePlanAssignmentStatus.CREATED -> return next == ManualServicePlanAssignmentStatus.PUBLISHED
            ManualServicePlanAssignmentStatus.VALIDATED -> {
                when (next) {
                    ManualServicePlanAssignmentStatus.PUBLISHED, ManualServicePlanAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            ManualServicePlanAssignmentStatus.PUBLISHED -> {
                when (next) {
                    ManualServicePlanAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualServicePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): ManualServicePlanAssignment {
        return ManualServicePlanAssignment(
            code = code,
            title = title,
            amount = amount,
            channel = channel,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual service plan assignment in status created. */
fun newManualServicePlanAssignment(code: String, title: String): ManualServicePlanAssignment {
    return ManualServicePlanAssignment(
        code = code,
        title = title,
        amount = 0.0,
        channel = "",
        trailerId = "",
        status = ManualServicePlanAssignmentStatus.CREATED,
    )
}
