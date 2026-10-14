package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.text.normalize

/** Where a scorecard estimate is in its life cycle. */
enum class ScorecardEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scorecard estimate and its current status. */
data class ScorecardEstimate(
    var code: String,
    var label: String,
    var routeCode: String,
    var startsAt: Int,
    var unit: String,
    var status: ScorecardEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardEstimateStatus): Boolean {
        when (status) {
            ScorecardEstimateStatus.DRAFT -> return next == ScorecardEstimateStatus.PENDING || next == ScorecardEstimateStatus.SUSPENDED
            ScorecardEstimateStatus.PENDING -> {
                when (next) {
                    ScorecardEstimateStatus.ACTIVE, ScorecardEstimateStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardEstimateStatus.ACTIVE -> return next == ScorecardEstimateStatus.SUSPENDED || next == ScorecardEstimateStatus.CLOSED
            ScorecardEstimateStatus.SUSPENDED -> return next == ScorecardEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScorecardEstimateStatus) {
        if (!canMoveTo(next)) {
            error("scorecard estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scorecard estimate"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): ScorecardEstimate {
        return ScorecardEstimate(
            code = code,
            label = label,
            routeCode = routeCode,
            startsAt = startsAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scorecard estimate in status draft. */
fun newScorecardEstimate(code: String, label: String): ScorecardEstimate {
    return ScorecardEstimate(
        code = code,
        label = label,
        routeCode = "",
        startsAt = 0,
        unit = "",
        status = ScorecardEstimateStatus.DRAFT,
    )
}
