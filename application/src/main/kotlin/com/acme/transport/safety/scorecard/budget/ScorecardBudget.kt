package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard budget records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard budget is in its life cycle. */
enum class ScorecardBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scorecard budget, with what the planners need to act on it. */
data class ScorecardBudget(
    var code: String,
    var name: String,
    var batchSize: Int,
    var ownerId: String,
    var maximum: Double,
    var segment: String,
    var dock: String,
    var pinned: Boolean,
    var driverId: String,
    var status: ScorecardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardBudgetStatus): Boolean {
        when (status) {
            ScorecardBudgetStatus.CREATED -> return next == ScorecardBudgetStatus.VALIDATED
            ScorecardBudgetStatus.VALIDATED -> return next == ScorecardBudgetStatus.RETIRED
            ScorecardBudgetStatus.PUBLISHED -> return next == ScorecardBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScorecardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scorecard budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((batchSize.toDouble() - maximum) * 100) / 100

    fun snapshot(): ScorecardBudget {
        return ScorecardBudget(
            code = code,
            name = name,
            batchSize = batchSize,
            ownerId = ownerId,
            maximum = maximum,
            segment = segment,
            dock = dock,
            pinned = pinned,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scorecard budget in status created. */
fun newScorecardBudget(code: String, name: String): ScorecardBudget {
    return ScorecardBudget(
        code = code,
        name = name,
        batchSize = 0,
        ownerId = "",
        maximum = 0.0,
        segment = "",
        dock = "",
        pinned = false,
        driverId = "",
        status = ScorecardBudgetStatus.CREATED,
    )
}
