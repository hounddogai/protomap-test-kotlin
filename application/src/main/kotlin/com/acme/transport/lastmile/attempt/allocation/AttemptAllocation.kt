package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt allocation records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt allocation is in its life cycle. */
enum class AttemptAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One attempt allocation, with what the planners need to act on it. */
data class AttemptAllocation(
    var code: String,
    var title: String,
    var threshold: Double,
    var maximum: Double,
    var margin: Double,
    var retries: Int,
    var reviewer: String,
    var lane: String,
    var odometer: Int,
    var status: AttemptAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptAllocationStatus): Boolean {
        when (status) {
            AttemptAllocationStatus.OPEN -> return next == AttemptAllocationStatus.IN_REVIEW || next == AttemptAllocationStatus.REOPENED
            AttemptAllocationStatus.IN_REVIEW -> return next == AttemptAllocationStatus.RESOLVED || next == AttemptAllocationStatus.ARCHIVED
            AttemptAllocationStatus.RESOLVED -> return next == AttemptAllocationStatus.REOPENED
            AttemptAllocationStatus.REOPENED -> {
                when (next) {
                    AttemptAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptAllocationStatus) {
        if (!canMoveTo(next)) {
            error("attempt allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((maximum - margin) * 100) / 100

    fun snapshot(): AttemptAllocation {
        return AttemptAllocation(
            code = code,
            title = title,
            threshold = threshold,
            maximum = maximum,
            margin = margin,
            retries = retries,
            reviewer = reviewer,
            lane = lane,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a attempt allocation in status open. */
fun newAttemptAllocation(code: String, title: String): AttemptAllocation {
    return AttemptAllocation(
        code = code,
        title = title,
        threshold = 0.0,
        maximum = 0.0,
        margin = 0.0,
        retries = 0,
        reviewer = "",
        lane = "",
        odometer = 0,
        status = AttemptAllocationStatus.OPEN,
    )
}
