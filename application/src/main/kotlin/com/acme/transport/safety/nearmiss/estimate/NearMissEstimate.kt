package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.normalize

// The near miss estimate records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss estimate is in its life cycle. */
enum class NearMissEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a near miss estimate and its current status. */
data class NearMissEstimate(
    var code: String,
    var label: String,
    var vehicleId: String,
    var notes: String,
    var windowDays: Int,
    var status: NearMissEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissEstimateStatus): Boolean {
        when (status) {
            NearMissEstimateStatus.OPEN -> {
                when (next) {
                    NearMissEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            NearMissEstimateStatus.IN_REVIEW -> {
                when (next) {
                    NearMissEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissEstimateStatus.RESOLVED -> {
                when (next) {
                    NearMissEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissEstimateStatus.REOPENED -> {
                when (next) {
                    NearMissEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissEstimateStatus) {
        if (!canMoveTo(next)) {
            error("near miss estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): NearMissEstimate {
        return NearMissEstimate(
            code = code,
            label = label,
            vehicleId = vehicleId,
            notes = notes,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a near miss estimate in status open. */
fun newNearMissEstimate(code: String, label: String): NearMissEstimate {
    return NearMissEstimate(
        code = code,
        label = label,
        vehicleId = "",
        notes = "",
        windowDays = 0,
        status = NearMissEstimateStatus.OPEN,
    )
}
