package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive violation score is in its life cycle. */
enum class AdaptiveViolationScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive violation score as the violation team keeps it. */
data class AdaptiveViolationScore(
    var code: String,
    var label: String,
    var minimum: Double,
    var priority: Int,
    var distanceKm: Double,
    var lane: String,
    var currency: String,
    var status: AdaptiveViolationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveViolationScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveViolationScoreStatus): Boolean {
        when (status) {
            AdaptiveViolationScoreStatus.PROPOSED -> return next == AdaptiveViolationScoreStatus.WITHDRAWN
            AdaptiveViolationScoreStatus.APPROVED -> return next == AdaptiveViolationScoreStatus.REJECTED || next == AdaptiveViolationScoreStatus.WITHDRAWN
            AdaptiveViolationScoreStatus.REJECTED -> {
                when (next) {
                    AdaptiveViolationScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveViolationScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive violation score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((priority.toDouble() - minimum) * 100) / 100

    fun snapshot(): AdaptiveViolationScore {
        return AdaptiveViolationScore(
            code = code,
            label = label,
            minimum = minimum,
            priority = priority,
            distanceKm = distanceKm,
            lane = lane,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive violation score in status proposed. */
fun newAdaptiveViolationScore(code: String, label: String): AdaptiveViolationScore {
    return AdaptiveViolationScore(
        code = code,
        label = label,
        minimum = 0.0,
        priority = 0,
        distanceKm = 0.0,
        lane = "",
        currency = "",
        status = AdaptiveViolationScoreStatus.PROPOSED,
    )
}
