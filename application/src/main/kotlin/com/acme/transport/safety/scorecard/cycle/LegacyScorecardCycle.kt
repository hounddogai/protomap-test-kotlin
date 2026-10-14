package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy scorecard cycle records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a legacy scorecard cycle is in its life cycle. */
enum class LegacyScorecardCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy scorecard cycle, with what the planners need to act on it. */
data class LegacyScorecardCycle(
    var code: String,
    var label: String,
    var externalRef: String,
    var threshold: Double,
    var cost: Double,
    var checksum: String,
    var sequence: Int,
    var status: LegacyScorecardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyScorecardCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyScorecardCycleStatus): Boolean {
        when (status) {
            LegacyScorecardCycleStatus.PROPOSED -> {
                when (next) {
                    LegacyScorecardCycleStatus.APPROVED, LegacyScorecardCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyScorecardCycleStatus.APPROVED -> return next == LegacyScorecardCycleStatus.WITHDRAWN
            LegacyScorecardCycleStatus.REJECTED -> return next == LegacyScorecardCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyScorecardCycleStatus) {
        if (!canMoveTo(next)) {
            error("legacy scorecard cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy scorecard cycle"} $code"

    fun effectiveTotal(): Double = round((cost + threshold) * 100) / 100

    fun snapshot(): LegacyScorecardCycle {
        return LegacyScorecardCycle(
            code = code,
            label = label,
            externalRef = externalRef,
            threshold = threshold,
            cost = cost,
            checksum = checksum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy scorecard cycle in status proposed. */
fun newLegacyScorecardCycle(code: String, label: String): LegacyScorecardCycle {
    return LegacyScorecardCycle(
        code = code,
        label = label,
        externalRef = "",
        threshold = 0.0,
        cost = 0.0,
        checksum = "",
        sequence = 0,
        status = LegacyScorecardCycleStatus.PROPOSED,
    )
}
