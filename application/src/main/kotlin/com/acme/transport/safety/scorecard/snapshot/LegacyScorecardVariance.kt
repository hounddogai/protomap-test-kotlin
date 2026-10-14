package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy scorecard variance is in its life cycle. */
enum class LegacyScorecardVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy scorecard variance and its current status. */
data class LegacyScorecardVariance(
    var code: String,
    var name: String,
    var cost: Double,
    var contactEmail: String,
    var maximum: Double,
    var status: LegacyScorecardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyScorecardVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyScorecardVarianceStatus): Boolean {
        when (status) {
            LegacyScorecardVarianceStatus.PROPOSED -> {
                when (next) {
                    LegacyScorecardVarianceStatus.APPROVED, LegacyScorecardVarianceStatus.REJECTED -> return true
                }
                return false
            }
            LegacyScorecardVarianceStatus.APPROVED -> {
                when (next) {
                    LegacyScorecardVarianceStatus.REJECTED, LegacyScorecardVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyScorecardVarianceStatus.REJECTED -> {
                when (next) {
                    LegacyScorecardVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyScorecardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy scorecard variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy scorecard variance"} $code"

    fun weightedValue(): Double = round(cost * maximum * 100) / 100

    fun snapshot(): LegacyScorecardVariance {
        return LegacyScorecardVariance(
            code = code,
            name = name,
            cost = cost,
            contactEmail = contactEmail,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy scorecard variance in status proposed. */
fun newLegacyScorecardVariance(code: String, name: String): LegacyScorecardVariance {
    return LegacyScorecardVariance(
        code = code,
        name = name,
        cost = 0.0,
        contactEmail = "",
        maximum = 0.0,
        status = LegacyScorecardVarianceStatus.PROPOSED,
    )
}
