package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service plan assignment is in its life cycle. */
enum class StrictServicePlanAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict service plan assignment. Its code is unique within a tenant. */
data class StrictServicePlanAssignment(
    var code: String,
    var title: String,
    var archived: Boolean,
    var sequence: Int,
    var quantity: Int,
    var threshold: Double,
    var capacity: Int,
    var status: StrictServicePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServicePlanAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServicePlanAssignmentStatus): Boolean {
        when (status) {
            StrictServicePlanAssignmentStatus.PLANNED -> {
                when (next) {
                    StrictServicePlanAssignmentStatus.COMPLETED -> return true
                }
                return false
            }
            StrictServicePlanAssignmentStatus.SCHEDULED -> return next == StrictServicePlanAssignmentStatus.IN_PROGRESS
            StrictServicePlanAssignmentStatus.IN_PROGRESS -> return next == StrictServicePlanAssignmentStatus.COMPLETED
            StrictServicePlanAssignmentStatus.COMPLETED -> return next == StrictServicePlanAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictServicePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("strict service plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((sequence.toDouble() + threshold) * 100) / 100

    fun snapshot(): StrictServicePlanAssignment {
        return StrictServicePlanAssignment(
            code = code,
            title = title,
            archived = archived,
            sequence = sequence,
            quantity = quantity,
            threshold = threshold,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict service plan assignment in status planned. */
fun newStrictServicePlanAssignment(code: String, title: String): StrictServicePlanAssignment {
    return StrictServicePlanAssignment(
        code = code,
        title = title,
        archived = false,
        sequence = 0,
        quantity = 0,
        threshold = 0.0,
        capacity = 0,
        status = StrictServicePlanAssignmentStatus.PLANNED,
    )
}
