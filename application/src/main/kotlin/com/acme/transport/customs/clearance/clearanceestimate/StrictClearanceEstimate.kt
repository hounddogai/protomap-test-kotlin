package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize

// The strict clearance estimate records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a strict clearance estimate is in its life cycle. */
enum class StrictClearanceEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict clearance estimate and its current status. */
data class StrictClearanceEstimate(
    var code: String,
    var name: String,
    var shiftCode: String,
    var title: String,
    var depot: String,
    var trailerId: String,
    var carrier: String,
    var limit: Int,
    var status: StrictClearanceEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictClearanceEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictClearanceEstimateStatus): Boolean {
        when (status) {
            StrictClearanceEstimateStatus.OPEN -> return next == StrictClearanceEstimateStatus.RESOLVED || next == StrictClearanceEstimateStatus.ARCHIVED
            StrictClearanceEstimateStatus.IN_REVIEW -> return next == StrictClearanceEstimateStatus.RESOLVED
            StrictClearanceEstimateStatus.RESOLVED -> {
                when (next) {
                    StrictClearanceEstimateStatus.REOPENED -> return true
                }
                return false
            }
            StrictClearanceEstimateStatus.REOPENED -> {
                when (next) {
                    StrictClearanceEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictClearanceEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict clearance estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict clearance estimate"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): StrictClearanceEstimate {
        return StrictClearanceEstimate(
            code = code,
            name = name,
            shiftCode = shiftCode,
            title = title,
            depot = depot,
            trailerId = trailerId,
            carrier = carrier,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict clearance estimate in status open. */
fun newStrictClearanceEstimate(code: String, name: String): StrictClearanceEstimate {
    return StrictClearanceEstimate(
        code = code,
        name = name,
        shiftCode = "",
        title = "",
        depot = "",
        trailerId = "",
        carrier = "",
        limit = 0,
        status = StrictClearanceEstimateStatus.OPEN,
    )
}
