package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching assignment records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching assignment is in its life cycle. */
enum class CoachingAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A coaching assignment as the coaching team keeps it. */
data class CoachingAssignment(
    var code: String,
    var title: String,
    var quantity: Int,
    var region: String,
    var description: String,
    var etaMinutes: Int,
    var distanceKm: Double,
    var status: CoachingAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingAssignmentStatus): Boolean {
        when (status) {
            CoachingAssignmentStatus.OPEN -> {
                when (next) {
                    CoachingAssignmentStatus.IN_REVIEW, CoachingAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingAssignmentStatus.IN_REVIEW -> return next == CoachingAssignmentStatus.REOPENED
            CoachingAssignmentStatus.RESOLVED -> {
                when (next) {
                    CoachingAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            CoachingAssignmentStatus.REOPENED -> return next == CoachingAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CoachingAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("coaching assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching assignment"} $code"

    fun projectedVolume(): Double = round((quantity.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CoachingAssignment {
        return CoachingAssignment(
            code = code,
            title = title,
            quantity = quantity,
            region = region,
            description = description,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a coaching assignment in status open. */
fun newCoachingAssignment(code: String, title: String): CoachingAssignment {
    return CoachingAssignment(
        code = code,
        title = title,
        quantity = 0,
        region = "",
        description = "",
        etaMinutes = 0,
        distanceKm = 0.0,
        status = CoachingAssignmentStatus.OPEN,
    )
}
