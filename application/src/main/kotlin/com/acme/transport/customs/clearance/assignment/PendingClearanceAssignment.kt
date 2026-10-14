package com.acme.transport.customs.clearance.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending clearance assignment records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a pending clearance assignment is in its life cycle. */
enum class PendingClearanceAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending clearance assignment, with what the planners need to act on it. */
data class PendingClearanceAssignment(
    var code: String,
    var label: String,
    var score: Double,
    var revision: Int,
    var attempts: Int,
    var shiftCode: String,
    var description: String,
    var status: PendingClearanceAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingClearanceAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingClearanceAssignmentStatus): Boolean {
        when (status) {
            PendingClearanceAssignmentStatus.PLANNED -> {
                when (next) {
                    PendingClearanceAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            PendingClearanceAssignmentStatus.SCHEDULED -> return next == PendingClearanceAssignmentStatus.IN_PROGRESS
            PendingClearanceAssignmentStatus.IN_PROGRESS -> return next == PendingClearanceAssignmentStatus.COMPLETED
            PendingClearanceAssignmentStatus.COMPLETED -> {
                when (next) {
                    PendingClearanceAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingClearanceAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending clearance assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round(score * revision.toDouble() * 100) / 100

    fun snapshot(): PendingClearanceAssignment {
        return PendingClearanceAssignment(
            code = code,
            label = label,
            score = score,
            revision = revision,
            attempts = attempts,
            shiftCode = shiftCode,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending clearance assignment in status planned. */
fun newPendingClearanceAssignment(code: String, label: String): PendingClearanceAssignment {
    return PendingClearanceAssignment(
        code = code,
        label = label,
        score = 0.0,
        revision = 0,
        attempts = 0,
        shiftCode = "",
        description = "",
        status = PendingClearanceAssignmentStatus.PLANNED,
    )
}
