package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive trailer summary records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a adaptive trailer summary is in its life cycle. */
enum class AdaptiveTrailerSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive trailer summary, with what the planners need to act on it. */
data class AdaptiveTrailerSummary(
    var code: String,
    var name: String,
    var windowDays: Int,
    var channel: String,
    var quantity: Int,
    var ceiling: Double,
    var status: AdaptiveTrailerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerSummaryStatus): Boolean {
        when (status) {
            AdaptiveTrailerSummaryStatus.PLANNED -> return next == AdaptiveTrailerSummaryStatus.CANCELLED
            AdaptiveTrailerSummaryStatus.SCHEDULED -> return next == AdaptiveTrailerSummaryStatus.IN_PROGRESS || next == AdaptiveTrailerSummaryStatus.COMPLETED
            AdaptiveTrailerSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveTrailerSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveTrailerSummaryStatus.COMPLETED -> {
                when (next) {
                    AdaptiveTrailerSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(windowDays.toDouble() * ceiling * 100) / 100

    fun snapshot(): AdaptiveTrailerSummary {
        return AdaptiveTrailerSummary(
            code = code,
            name = name,
            windowDays = windowDays,
            channel = channel,
            quantity = quantity,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive trailer summary in status planned. */
fun newAdaptiveTrailerSummary(code: String, name: String): AdaptiveTrailerSummary {
    return AdaptiveTrailerSummary(
        code = code,
        name = name,
        windowDays = 0,
        channel = "",
        quantity = 0,
        ceiling = 0.0,
        status = AdaptiveTrailerSummaryStatus.PLANNED,
    )
}
