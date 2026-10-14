package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance assignment is in its life cycle. */
enum class ClearanceAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A clearance assignment as the clearance team keeps it. */
data class ClearanceAssignment(
    var code: String,
    var title: String,
    var endsAt: Int,
    var threshold: Double,
    var approved: Boolean,
    var status: ClearanceAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceAssignmentStatus): Boolean {
        when (status) {
            ClearanceAssignmentStatus.PLANNED -> return next == ClearanceAssignmentStatus.COMPLETED
            ClearanceAssignmentStatus.SCHEDULED -> {
                when (next) {
                    ClearanceAssignmentStatus.IN_PROGRESS, ClearanceAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            ClearanceAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    ClearanceAssignmentStatus.COMPLETED -> return true
                }
                return false
            }
            ClearanceAssignmentStatus.COMPLETED -> {
                when (next) {
                    ClearanceAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("clearance assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"clearance assignment"} $code"

    fun adjustedLoad(): Double = round((threshold - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ClearanceAssignment {
        return ClearanceAssignment(
            code = code,
            title = title,
            endsAt = endsAt,
            threshold = threshold,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance assignment in status planned. */
fun newClearanceAssignment(code: String, title: String): ClearanceAssignment {
    return ClearanceAssignment(
        code = code,
        title = title,
        endsAt = 0,
        threshold = 0.0,
        approved = false,
        status = ClearanceAssignmentStatus.PLANNED,
    )
}
