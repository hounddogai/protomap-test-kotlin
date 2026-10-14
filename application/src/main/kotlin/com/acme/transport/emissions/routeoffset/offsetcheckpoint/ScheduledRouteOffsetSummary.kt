package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled route offset summary is in its life cycle. */
enum class ScheduledRouteOffsetSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled route offset summary and its current status. */
data class ScheduledRouteOffsetSummary(
    var code: String,
    var name: String,
    var stopCount: Int,
    var title: String,
    var shiftCode: String,
    var margin: Double,
    var unit: String,
    var capacity: Int,
    var weight: Double,
    var pallets: Int,
    var status: ScheduledRouteOffsetSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetSummaryStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetSummaryStatus.OPEN -> {
                when (next) {
                    ScheduledRouteOffsetSummaryStatus.RESOLVED, ScheduledRouteOffsetSummaryStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledRouteOffsetSummaryStatus.IN_REVIEW -> return next == ScheduledRouteOffsetSummaryStatus.REOPENED
            ScheduledRouteOffsetSummaryStatus.RESOLVED -> {
                when (next) {
                    ScheduledRouteOffsetSummaryStatus.REOPENED, ScheduledRouteOffsetSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledRouteOffsetSummaryStatus.REOPENED -> return next == ScheduledRouteOffsetSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((capacity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRouteOffsetSummary {
        return ScheduledRouteOffsetSummary(
            code = code,
            name = name,
            stopCount = stopCount,
            title = title,
            shiftCode = shiftCode,
            margin = margin,
            unit = unit,
            capacity = capacity,
            weight = weight,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled route offset summary in status open. */
fun newScheduledRouteOffsetSummary(code: String, name: String): ScheduledRouteOffsetSummary {
    return ScheduledRouteOffsetSummary(
        code = code,
        name = name,
        stopCount = 0,
        title = "",
        shiftCode = "",
        margin = 0.0,
        unit = "",
        capacity = 0,
        weight = 0.0,
        pallets = 0,
        status = ScheduledRouteOffsetSummaryStatus.OPEN,
    )
}
