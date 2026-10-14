package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard rollup records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard rollup is in its life cycle. */
enum class ScorecardRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scorecard rollup and its current status. */
data class ScorecardRollup(
    var code: String,
    var label: String,
    var priority: Int,
    var amount: Double,
    var routeCode: String,
    var status: ScorecardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardRollupStatus): Boolean {
        when (status) {
            ScorecardRollupStatus.PROPOSED -> return next == ScorecardRollupStatus.REJECTED || next == ScorecardRollupStatus.WITHDRAWN
            ScorecardRollupStatus.APPROVED -> {
                when (next) {
                    ScorecardRollupStatus.REJECTED, ScorecardRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardRollupStatus.REJECTED -> return next == ScorecardRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScorecardRollupStatus) {
        if (!canMoveTo(next)) {
            error("scorecard rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((amount - priority.toDouble()) * 100) / 100

    fun snapshot(): ScorecardRollup {
        return ScorecardRollup(
            code = code,
            label = label,
            priority = priority,
            amount = amount,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard rollup in status proposed. */
fun newScorecardRollup(code: String, label: String): ScorecardRollup {
    return ScorecardRollup(
        code = code,
        label = label,
        priority = 0,
        amount = 0.0,
        routeCode = "",
        status = ScorecardRollupStatus.PROPOSED,
    )
}
