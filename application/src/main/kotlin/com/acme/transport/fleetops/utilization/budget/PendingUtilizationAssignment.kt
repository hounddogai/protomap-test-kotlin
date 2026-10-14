package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize

/** Where a pending utilization assignment is in its life cycle. */
enum class PendingUtilizationAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending utilization assignment and its current status. */
data class PendingUtilizationAssignment(
    var code: String,
    var label: String,
    var ownerId: String,
    var graceDays: Int,
    var carrier: String,
    var summary: String,
    var active: Boolean,
    var status: PendingUtilizationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingUtilizationAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingUtilizationAssignmentStatus): Boolean {
        when (status) {
            PendingUtilizationAssignmentStatus.REQUESTED -> {
                when (next) {
                    PendingUtilizationAssignmentStatus.REVOKED, PendingUtilizationAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            PendingUtilizationAssignmentStatus.GRANTED -> {
                when (next) {
                    PendingUtilizationAssignmentStatus.REVOKED, PendingUtilizationAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            PendingUtilizationAssignmentStatus.REVOKED -> {
                when (next) {
                    PendingUtilizationAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingUtilizationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending utilization assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): PendingUtilizationAssignment {
        return PendingUtilizationAssignment(
            code = code,
            label = label,
            ownerId = ownerId,
            graceDays = graceDays,
            carrier = carrier,
            summary = summary,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending utilization assignment in status requested. */
fun newPendingUtilizationAssignment(code: String, label: String): PendingUtilizationAssignment {
    return PendingUtilizationAssignment(
        code = code,
        label = label,
        ownerId = "",
        graceDays = 0,
        carrier = "",
        summary = "",
        active = false,
        status = PendingUtilizationAssignmentStatus.REQUESTED,
    )
}
