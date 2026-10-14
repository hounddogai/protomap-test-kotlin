package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending scorecard assignment records of safety's scorecard work, and the rules, stores, and
// calculations around them.

/** Where a pending scorecard assignment is in its life cycle. */
enum class PendingScorecardAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending scorecard assignment and its current status. */
data class PendingScorecardAssignment(
    var code: String,
    var title: String,
    var threshold: Double,
    var active: Boolean,
    var contactEmail: String,
    var dock: String,
    var discount: Double,
    var status: PendingScorecardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingScorecardAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingScorecardAssignmentStatus): Boolean {
        when (status) {
            PendingScorecardAssignmentStatus.PROPOSED -> return next == PendingScorecardAssignmentStatus.APPROVED || next == PendingScorecardAssignmentStatus.REJECTED
            PendingScorecardAssignmentStatus.APPROVED -> return next == PendingScorecardAssignmentStatus.REJECTED || next == PendingScorecardAssignmentStatus.WITHDRAWN
            PendingScorecardAssignmentStatus.REJECTED -> {
                when (next) {
                    PendingScorecardAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingScorecardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending scorecard assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending scorecard assignment"} $code"

    fun weightedVolume(): Double = round((discount - threshold) * 100) / 100

    fun snapshot(): PendingScorecardAssignment {
        return PendingScorecardAssignment(
            code = code,
            title = title,
            threshold = threshold,
            active = active,
            contactEmail = contactEmail,
            dock = dock,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending scorecard assignment in status proposed. */
fun newPendingScorecardAssignment(code: String, title: String): PendingScorecardAssignment {
    return PendingScorecardAssignment(
        code = code,
        title = title,
        threshold = 0.0,
        active = false,
        contactEmail = "",
        dock = "",
        discount = 0.0,
        status = PendingScorecardAssignmentStatus.PROPOSED,
    )
}
