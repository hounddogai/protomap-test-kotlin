package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.text.normalize

/** Where a scorecard metric is in its life cycle. */
enum class ScorecardMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scorecard metric and its current status. */
data class ScorecardMetric(
    var code: String,
    var title: String,
    var checksum: String,
    var unit: String,
    var attempts: Int,
    var timezone: String,
    var status: ScorecardMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardMetricStatus): Boolean {
        when (status) {
            ScorecardMetricStatus.REQUESTED -> {
                when (next) {
                    ScorecardMetricStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardMetricStatus.GRANTED -> {
                when (next) {
                    ScorecardMetricStatus.REVOKED, ScorecardMetricStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardMetricStatus.REVOKED -> return next == ScorecardMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardMetricStatus) {
        if (!canMoveTo(next)) {
            error("scorecard metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scorecard metric"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): ScorecardMetric {
        return ScorecardMetric(
            code = code,
            title = title,
            checksum = checksum,
            unit = unit,
            attempts = attempts,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scorecard metric in status requested. */
fun newScorecardMetric(code: String, title: String): ScorecardMetric {
    return ScorecardMetric(
        code = code,
        title = title,
        checksum = "",
        unit = "",
        attempts = 0,
        timezone = "",
        status = ScorecardMetricStatus.REQUESTED,
    )
}
