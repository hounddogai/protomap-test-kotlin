package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize

/** Where a default scorecard assignment is in its life cycle. */
enum class DefaultScorecardAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default scorecard assignment and its current status. */
data class DefaultScorecardAssignment(
    var code: String,
    var name: String,
    var startsAt: Int,
    var trailerId: String,
    var depot: String,
    var checksum: String,
    var status: DefaultScorecardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardAssignmentStatus): Boolean {
        when (status) {
            DefaultScorecardAssignmentStatus.PROPOSED -> return next == DefaultScorecardAssignmentStatus.APPROVED
            DefaultScorecardAssignmentStatus.APPROVED -> return next == DefaultScorecardAssignmentStatus.REJECTED
            DefaultScorecardAssignmentStatus.REJECTED -> return next == DefaultScorecardAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultScorecardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): DefaultScorecardAssignment {
        return DefaultScorecardAssignment(
            code = code,
            name = name,
            startsAt = startsAt,
            trailerId = trailerId,
            depot = depot,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default scorecard assignment in status proposed. */
fun newDefaultScorecardAssignment(code: String, name: String): DefaultScorecardAssignment {
    return DefaultScorecardAssignment(
        code = code,
        name = name,
        startsAt = 0,
        trailerId = "",
        depot = "",
        checksum = "",
        status = DefaultScorecardAssignmentStatus.PROPOSED,
    )
}
