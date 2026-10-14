package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard threshold records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard threshold is in its life cycle. */
enum class ScorecardThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scorecard threshold as the scorecard team keeps it. */
data class ScorecardThreshold(
    var code: String,
    var title: String,
    var currency: String,
    var attempts: Int,
    var maximum: Double,
    var status: ScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardThresholdStatus): Boolean {
        when (status) {
            ScorecardThresholdStatus.OPEN -> {
                when (next) {
                    ScorecardThresholdStatus.REOPENED, ScorecardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardThresholdStatus.IN_REVIEW -> {
                when (next) {
                    ScorecardThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            ScorecardThresholdStatus.RESOLVED -> {
                when (next) {
                    ScorecardThresholdStatus.REOPENED, ScorecardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardThresholdStatus.REOPENED -> {
                when (next) {
                    ScorecardThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("scorecard threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round(attempts.toDouble() * maximum * 100) / 100

    fun snapshot(): ScorecardThreshold {
        return ScorecardThreshold(
            code = code,
            title = title,
            currency = currency,
            attempts = attempts,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard threshold in status open. */
fun newScorecardThreshold(code: String, title: String): ScorecardThreshold {
    return ScorecardThreshold(
        code = code,
        title = title,
        currency = "",
        attempts = 0,
        maximum = 0.0,
        status = ScorecardThresholdStatus.OPEN,
    )
}
