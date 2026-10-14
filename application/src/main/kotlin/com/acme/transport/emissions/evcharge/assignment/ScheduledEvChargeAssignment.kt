package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled ev charge assignment records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a scheduled ev charge assignment is in its life cycle. */
enum class ScheduledEvChargeAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled ev charge assignment and its current status. */
data class ScheduledEvChargeAssignment(
    var code: String,
    var label: String,
    var ceiling: Double,
    var category: String,
    var amount: Double,
    var status: ScheduledEvChargeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEvChargeAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEvChargeAssignmentStatus): Boolean {
        when (status) {
            ScheduledEvChargeAssignmentStatus.PLANNED -> return next == ScheduledEvChargeAssignmentStatus.IN_PROGRESS || next == ScheduledEvChargeAssignmentStatus.CANCELLED
            ScheduledEvChargeAssignmentStatus.SCHEDULED -> return next == ScheduledEvChargeAssignmentStatus.IN_PROGRESS || next == ScheduledEvChargeAssignmentStatus.CANCELLED
            ScheduledEvChargeAssignmentStatus.IN_PROGRESS -> return next == ScheduledEvChargeAssignmentStatus.COMPLETED || next == ScheduledEvChargeAssignmentStatus.CANCELLED
            ScheduledEvChargeAssignmentStatus.COMPLETED -> return next == ScheduledEvChargeAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledEvChargeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled ev charge assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled ev charge assignment"} $code"

    fun projectedValue(): Double = round((amount - ceiling) * 100) / 100

    fun snapshot(): ScheduledEvChargeAssignment {
        return ScheduledEvChargeAssignment(
            code = code,
            label = label,
            ceiling = ceiling,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled ev charge assignment in status planned. */
fun newScheduledEvChargeAssignment(code: String, label: String): ScheduledEvChargeAssignment {
    return ScheduledEvChargeAssignment(
        code = code,
        label = label,
        ceiling = 0.0,
        category = "",
        amount = 0.0,
        status = ScheduledEvChargeAssignmentStatus.PLANNED,
    )
}
