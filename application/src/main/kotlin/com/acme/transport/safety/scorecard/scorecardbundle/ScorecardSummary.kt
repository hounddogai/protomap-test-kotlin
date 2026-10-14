package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard summary is in its life cycle. */
enum class ScorecardSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scorecard summary and its current status. */
data class ScorecardSummary(
    var code: String,
    var name: String,
    var windowDays: Int,
    var graceDays: Int,
    var distanceKm: Double,
    var loadKg: Double,
    var ceiling: Double,
    var status: ScorecardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardSummaryStatus): Boolean {
        when (status) {
            ScorecardSummaryStatus.IDLE -> return next == ScorecardSummaryStatus.WARMING || next == ScorecardSummaryStatus.DRAINING
            ScorecardSummaryStatus.WARMING -> {
                when (next) {
                    ScorecardSummaryStatus.READY -> return true
                }
                return false
            }
            ScorecardSummaryStatus.READY -> return next == ScorecardSummaryStatus.DRAINING || next == ScorecardSummaryStatus.STOPPED
            ScorecardSummaryStatus.DRAINING -> return next == ScorecardSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScorecardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scorecard summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scorecard summary"} $code"

    fun adjustedExposure(): Double = round((windowDays.toDouble() - loadKg) * 100) / 100

    fun snapshot(): ScorecardSummary {
        return ScorecardSummary(
            code = code,
            name = name,
            windowDays = windowDays,
            graceDays = graceDays,
            distanceKm = distanceKm,
            loadKg = loadKg,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard summary in status idle. */
fun newScorecardSummary(code: String, name: String): ScorecardSummary {
    return ScorecardSummary(
        code = code,
        name = name,
        windowDays = 0,
        graceDays = 0,
        distanceKm = 0.0,
        loadKg = 0.0,
        ceiling = 0.0,
        status = ScorecardSummaryStatus.IDLE,
    )
}
