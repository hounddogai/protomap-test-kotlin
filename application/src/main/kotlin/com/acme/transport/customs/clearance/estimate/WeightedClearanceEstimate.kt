package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.normalize

// The weighted clearance estimate records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a weighted clearance estimate is in its life cycle. */
enum class WeightedClearanceEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a weighted clearance estimate and its current status. */
data class WeightedClearanceEstimate(
    var code: String,
    var name: String,
    var active: Boolean,
    var label: String,
    var attempts: Int,
    var archived: Boolean,
    var status: WeightedClearanceEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedClearanceEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedClearanceEstimateStatus): Boolean {
        when (status) {
            WeightedClearanceEstimateStatus.OPEN -> return next == WeightedClearanceEstimateStatus.ARCHIVED
            WeightedClearanceEstimateStatus.IN_REVIEW -> return next == WeightedClearanceEstimateStatus.ARCHIVED
            WeightedClearanceEstimateStatus.RESOLVED -> return next == WeightedClearanceEstimateStatus.REOPENED
            WeightedClearanceEstimateStatus.REOPENED -> {
                when (next) {
                    WeightedClearanceEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedClearanceEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted clearance estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted clearance estimate"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): WeightedClearanceEstimate {
        return WeightedClearanceEstimate(
            code = code,
            name = name,
            active = active,
            label = label,
            attempts = attempts,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted clearance estimate in status open. */
fun newWeightedClearanceEstimate(code: String, name: String): WeightedClearanceEstimate {
    return WeightedClearanceEstimate(
        code = code,
        name = name,
        active = false,
        label = "",
        attempts = 0,
        archived = false,
        status = WeightedClearanceEstimateStatus.OPEN,
    )
}
