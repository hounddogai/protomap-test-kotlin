package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal trailer yard summary records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a seasonal trailer yard summary is in its life cycle. */
enum class SeasonalTrailerYardSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal trailer yard summary as the trailer yard team keeps it. */
data class SeasonalTrailerYardSummary(
    var code: String,
    var label: String,
    var lane: String,
    var minimum: Double,
    var retries: Int,
    var batchSize: Int,
    var maximum: Double,
    var driverId: String,
    var status: SeasonalTrailerYardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardSummaryStatus): Boolean {
        when (status) {
            SeasonalTrailerYardSummaryStatus.OPEN -> return next == SeasonalTrailerYardSummaryStatus.IN_REVIEW || next == SeasonalTrailerYardSummaryStatus.REOPENED
            SeasonalTrailerYardSummaryStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalTrailerYardSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalTrailerYardSummaryStatus.RESOLVED -> return next == SeasonalTrailerYardSummaryStatus.ARCHIVED
            SeasonalTrailerYardSummaryStatus.REOPENED -> return next == SeasonalTrailerYardSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal trailer yard summary"} $code"

    fun effectiveExposure(): Double = round(maximum * retries.toDouble() * 100) / 100

    fun snapshot(): SeasonalTrailerYardSummary {
        return SeasonalTrailerYardSummary(
            code = code,
            label = label,
            lane = lane,
            minimum = minimum,
            retries = retries,
            batchSize = batchSize,
            maximum = maximum,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal trailer yard summary in status open. */
fun newSeasonalTrailerYardSummary(code: String, label: String): SeasonalTrailerYardSummary {
    return SeasonalTrailerYardSummary(
        code = code,
        label = label,
        lane = "",
        minimum = 0.0,
        retries = 0,
        batchSize = 0,
        maximum = 0.0,
        driverId = "",
        status = SeasonalTrailerYardSummaryStatus.OPEN,
    )
}
