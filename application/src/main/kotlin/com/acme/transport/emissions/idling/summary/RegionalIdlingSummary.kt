package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional idling summary records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a regional idling summary is in its life cycle. */
enum class RegionalIdlingSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional idling summary, with what the planners need to act on it. */
data class RegionalIdlingSummary(
    var code: String,
    var name: String,
    var cost: Double,
    var retries: Int,
    var threshold: Double,
    var segment: String,
    var endsAt: Int,
    var floorValue: Double,
    var status: RegionalIdlingSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingSummaryStatus): Boolean {
        when (status) {
            RegionalIdlingSummaryStatus.PLANNED -> {
                when (next) {
                    RegionalIdlingSummaryStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RegionalIdlingSummaryStatus.SCHEDULED -> {
                when (next) {
                    RegionalIdlingSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalIdlingSummaryStatus.IN_PROGRESS -> return next == RegionalIdlingSummaryStatus.COMPLETED
            RegionalIdlingSummaryStatus.COMPLETED -> return next == RegionalIdlingSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalIdlingSummaryStatus) {
        if (!canMoveTo(next)) {
            error("regional idling summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round(endsAt.toDouble() * threshold * 100) / 100

    fun snapshot(): RegionalIdlingSummary {
        return RegionalIdlingSummary(
            code = code,
            name = name,
            cost = cost,
            retries = retries,
            threshold = threshold,
            segment = segment,
            endsAt = endsAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional idling summary in status planned. */
fun newRegionalIdlingSummary(code: String, name: String): RegionalIdlingSummary {
    return RegionalIdlingSummary(
        code = code,
        name = name,
        cost = 0.0,
        retries = 0,
        threshold = 0.0,
        segment = "",
        endsAt = 0,
        floorValue = 0.0,
        status = RegionalIdlingSummaryStatus.PLANNED,
    )
}
