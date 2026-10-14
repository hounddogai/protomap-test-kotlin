package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt assignment is in its life cycle. */
enum class AttemptAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a attempt assignment and its current status. */
data class AttemptAssignment(
    var code: String,
    var title: String,
    var description: String,
    var startsAt: Int,
    var ownerId: String,
    var loadKg: Double,
    var region: String,
    var status: AttemptAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptAssignmentStatus): Boolean {
        when (status) {
            AttemptAssignmentStatus.PROPOSED -> return next == AttemptAssignmentStatus.APPROVED || next == AttemptAssignmentStatus.WITHDRAWN
            AttemptAssignmentStatus.APPROVED -> return next == AttemptAssignmentStatus.REJECTED || next == AttemptAssignmentStatus.WITHDRAWN
            AttemptAssignmentStatus.REJECTED -> return next == AttemptAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("attempt assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round((startsAt.toDouble() + loadKg) * 100) / 100

    fun snapshot(): AttemptAssignment {
        return AttemptAssignment(
            code = code,
            title = title,
            description = description,
            startsAt = startsAt,
            ownerId = ownerId,
            loadKg = loadKg,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a attempt assignment in status proposed. */
fun newAttemptAssignment(code: String, title: String): AttemptAssignment {
    return AttemptAssignment(
        code = code,
        title = title,
        description = "",
        startsAt = 0,
        ownerId = "",
        loadKg = 0.0,
        region = "",
        status = AttemptAssignmentStatus.PROPOSED,
    )
}
