package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard assignment is in its life cycle. */
enum class ScorecardAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scorecard assignment, with what the planners need to act on it. */
data class ScorecardAssignment(
    var code: String,
    var label: String,
    var dock: String,
    var sequence: Int,
    var channel: String,
    var windowDays: Int,
    var lane: String,
    var durationMinutes: Int,
    var status: ScorecardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardAssignmentStatus): Boolean {
        when (status) {
            ScorecardAssignmentStatus.PROPOSED -> {
                when (next) {
                    ScorecardAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardAssignmentStatus.APPROVED -> {
                when (next) {
                    ScorecardAssignmentStatus.REJECTED, ScorecardAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardAssignmentStatus.REJECTED -> return next == ScorecardAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScorecardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scorecard assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((durationMinutes.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ScorecardAssignment {
        return ScorecardAssignment(
            code = code,
            label = label,
            dock = dock,
            sequence = sequence,
            channel = channel,
            windowDays = windowDays,
            lane = lane,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scorecard assignment in status proposed. */
fun newScorecardAssignment(code: String, label: String): ScorecardAssignment {
    return ScorecardAssignment(
        code = code,
        label = label,
        dock = "",
        sequence = 0,
        channel = "",
        windowDays = 0,
        lane = "",
        durationMinutes = 0,
        status = ScorecardAssignmentStatus.PROPOSED,
    )
}
