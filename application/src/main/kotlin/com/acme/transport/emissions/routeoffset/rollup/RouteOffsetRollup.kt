package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset rollup records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset rollup is in its life cycle. */
enum class RouteOffsetRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a route offset rollup and its current status. */
data class RouteOffsetRollup(
    var code: String,
    var title: String,
    var cost: Double,
    var expiresAt: Int,
    var threshold: Double,
    var notes: String,
    var fuelLiters: Double,
    var retries: Int,
    var ceiling: Double,
    var segment: String,
    var status: RouteOffsetRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetRollupStatus): Boolean {
        when (status) {
            RouteOffsetRollupStatus.PLANNED -> return next == RouteOffsetRollupStatus.SCHEDULED
            RouteOffsetRollupStatus.SCHEDULED -> return next == RouteOffsetRollupStatus.IN_PROGRESS || next == RouteOffsetRollupStatus.COMPLETED
            RouteOffsetRollupStatus.IN_PROGRESS -> return next == RouteOffsetRollupStatus.COMPLETED || next == RouteOffsetRollupStatus.CANCELLED
            RouteOffsetRollupStatus.COMPLETED -> return next == RouteOffsetRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RouteOffsetRollupStatus) {
        if (!canMoveTo(next)) {
            error("route offset rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round(retries.toDouble() * ceiling * 100) / 100

    fun snapshot(): RouteOffsetRollup {
        return RouteOffsetRollup(
            code = code,
            title = title,
            cost = cost,
            expiresAt = expiresAt,
            threshold = threshold,
            notes = notes,
            fuelLiters = fuelLiters,
            retries = retries,
            ceiling = ceiling,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a route offset rollup in status planned. */
fun newRouteOffsetRollup(code: String, title: String): RouteOffsetRollup {
    return RouteOffsetRollup(
        code = code,
        title = title,
        cost = 0.0,
        expiresAt = 0,
        threshold = 0.0,
        notes = "",
        fuelLiters = 0.0,
        retries = 0,
        ceiling = 0.0,
        segment = "",
        status = RouteOffsetRollupStatus.PLANNED,
    )
}
