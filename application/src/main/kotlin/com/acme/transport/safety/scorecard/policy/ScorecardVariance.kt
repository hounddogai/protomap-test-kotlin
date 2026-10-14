package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard variance is in its life cycle. */
enum class ScorecardVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scorecard variance. Its code is unique within a tenant. */
data class ScorecardVariance(
    var code: String,
    var title: String,
    var attempts: Int,
    var ceiling: Double,
    var source: String,
    var status: ScorecardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardVarianceStatus): Boolean {
        when (status) {
            ScorecardVarianceStatus.PLANNED -> {
                when (next) {
                    ScorecardVarianceStatus.SCHEDULED -> return true
                }
                return false
            }
            ScorecardVarianceStatus.SCHEDULED -> {
                when (next) {
                    ScorecardVarianceStatus.IN_PROGRESS, ScorecardVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            ScorecardVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    ScorecardVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            ScorecardVarianceStatus.COMPLETED -> return next == ScorecardVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScorecardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("scorecard variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scorecard variance"} $code"

    fun weightedValue(): Double = round(attempts.toDouble() * ceiling * 100) / 100

    fun snapshot(): ScorecardVariance {
        return ScorecardVariance(
            code = code,
            title = title,
            attempts = attempts,
            ceiling = ceiling,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scorecard variance in status planned. */
fun newScorecardVariance(code: String, title: String): ScorecardVariance {
    return ScorecardVariance(
        code = code,
        title = title,
        attempts = 0,
        ceiling = 0.0,
        source = "",
        status = ScorecardVarianceStatus.PLANNED,
    )
}
