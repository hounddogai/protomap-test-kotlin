package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection estimate records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection estimate is in its life cycle. */
enum class InspectionEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One inspection estimate, with what the planners need to act on it. */
data class InspectionEstimate(
    var code: String,
    var label: String,
    var minimum: Double,
    var graceDays: Int,
    var depot: String,
    var category: String,
    var timezone: String,
    var status: InspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionEstimateStatus): Boolean {
        when (status) {
            InspectionEstimateStatus.PROPOSED -> return next == InspectionEstimateStatus.APPROVED
            InspectionEstimateStatus.APPROVED -> return next == InspectionEstimateStatus.REJECTED || next == InspectionEstimateStatus.WITHDRAWN
            InspectionEstimateStatus.REJECTED -> {
                when (next) {
                    InspectionEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("inspection estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((graceDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): InspectionEstimate {
        return InspectionEstimate(
            code = code,
            label = label,
            minimum = minimum,
            graceDays = graceDays,
            depot = depot,
            category = category,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a inspection estimate in status proposed. */
fun newInspectionEstimate(code: String, label: String): InspectionEstimate {
    return InspectionEstimate(
        code = code,
        label = label,
        minimum = 0.0,
        graceDays = 0,
        depot = "",
        category = "",
        timezone = "",
        status = InspectionEstimateStatus.PROPOSED,
    )
}
