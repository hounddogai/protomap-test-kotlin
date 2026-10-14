package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset summary records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset summary is in its life cycle. */
enum class RouteOffsetSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One route offset summary, with what the planners need to act on it. */
data class RouteOffsetSummary(
    var code: String,
    var title: String,
    var stopCount: Int,
    var driverId: String,
    var batchSize: Int,
    var reviewer: String,
    var sequence: Int,
    var externalRef: String,
    var status: RouteOffsetSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetSummaryStatus): Boolean {
        when (status) {
            RouteOffsetSummaryStatus.PROPOSED -> return next == RouteOffsetSummaryStatus.WITHDRAWN
            RouteOffsetSummaryStatus.APPROVED -> {
                when (next) {
                    RouteOffsetSummaryStatus.REJECTED, RouteOffsetSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            RouteOffsetSummaryStatus.REJECTED -> {
                when (next) {
                    RouteOffsetSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetSummaryStatus) {
        if (!canMoveTo(next)) {
            error("route offset summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((batchSize.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetSummary {
        return RouteOffsetSummary(
            code = code,
            title = title,
            stopCount = stopCount,
            driverId = driverId,
            batchSize = batchSize,
            reviewer = reviewer,
            sequence = sequence,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset summary in status proposed. */
fun newRouteOffsetSummary(code: String, title: String): RouteOffsetSummary {
    return RouteOffsetSummary(
        code = code,
        title = title,
        stopCount = 0,
        driverId = "",
        batchSize = 0,
        reviewer = "",
        sequence = 0,
        externalRef = "",
        status = RouteOffsetSummaryStatus.PROPOSED,
    )
}
