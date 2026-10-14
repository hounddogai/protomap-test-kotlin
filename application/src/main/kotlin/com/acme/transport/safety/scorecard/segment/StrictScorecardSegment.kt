package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict scorecard segment records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a strict scorecard segment is in its life cycle. */
enum class StrictScorecardSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict scorecard segment, with what the planners need to act on it. */
data class StrictScorecardSegment(
    var code: String,
    var label: String,
    var graceDays: Int,
    var sequence: Int,
    var ratio: Double,
    var weight: Double,
    var archived: Boolean,
    var status: StrictScorecardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictScorecardSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictScorecardSegmentStatus): Boolean {
        when (status) {
            StrictScorecardSegmentStatus.PROPOSED -> {
                when (next) {
                    StrictScorecardSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictScorecardSegmentStatus.APPROVED -> return next == StrictScorecardSegmentStatus.REJECTED || next == StrictScorecardSegmentStatus.WITHDRAWN
            StrictScorecardSegmentStatus.REJECTED -> return next == StrictScorecardSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictScorecardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("strict scorecard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict scorecard segment"} $code"

    fun adjustedTotal(): Double = round((ratio + weight) * 100) / 100

    fun snapshot(): StrictScorecardSegment {
        return StrictScorecardSegment(
            code = code,
            label = label,
            graceDays = graceDays,
            sequence = sequence,
            ratio = ratio,
            weight = weight,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict scorecard segment in status proposed. */
fun newStrictScorecardSegment(code: String, label: String): StrictScorecardSegment {
    return StrictScorecardSegment(
        code = code,
        label = label,
        graceDays = 0,
        sequence = 0,
        ratio = 0.0,
        weight = 0.0,
        archived = false,
        status = StrictScorecardSegmentStatus.PROPOSED,
    )
}
