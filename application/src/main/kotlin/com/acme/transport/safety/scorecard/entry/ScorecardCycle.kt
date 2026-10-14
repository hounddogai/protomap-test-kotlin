package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard cycle is in its life cycle. */
enum class ScorecardCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scorecard cycle and its current status. */
data class ScorecardCycle(
    var code: String,
    var label: String,
    var minimum: Double,
    var windowDays: Int,
    var budget: Double,
    var locale: String,
    var status: ScorecardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardCycleStatus): Boolean {
        when (status) {
            ScorecardCycleStatus.CREATED -> return next == ScorecardCycleStatus.RETIRED
            ScorecardCycleStatus.VALIDATED -> {
                when (next) {
                    ScorecardCycleStatus.PUBLISHED, ScorecardCycleStatus.RETIRED -> return true
                }
                return false
            }
            ScorecardCycleStatus.PUBLISHED -> {
                when (next) {
                    ScorecardCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardCycleStatus) {
        if (!canMoveTo(next)) {
            error("scorecard cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((windowDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): ScorecardCycle {
        return ScorecardCycle(
            code = code,
            label = label,
            minimum = minimum,
            windowDays = windowDays,
            budget = budget,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scorecard cycle in status created. */
fun newScorecardCycle(code: String, label: String): ScorecardCycle {
    return ScorecardCycle(
        code = code,
        label = label,
        minimum = 0.0,
        windowDays = 0,
        budget = 0.0,
        locale = "",
        status = ScorecardCycleStatus.CREATED,
    )
}
