package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset override records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset override is in its life cycle. */
enum class RouteOffsetOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a route offset override and its current status. */
data class RouteOffsetOverride(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var margin: Double,
    var sequence: Int,
    var status: RouteOffsetOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetOverrideStatus): Boolean {
        when (status) {
            RouteOffsetOverrideStatus.OPEN -> {
                when (next) {
                    RouteOffsetOverrideStatus.IN_REVIEW, RouteOffsetOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetOverrideStatus.IN_REVIEW -> return next == RouteOffsetOverrideStatus.REOPENED
            RouteOffsetOverrideStatus.RESOLVED -> return next == RouteOffsetOverrideStatus.ARCHIVED
            RouteOffsetOverrideStatus.REOPENED -> {
                when (next) {
                    RouteOffsetOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetOverrideStatus) {
        if (!canMoveTo(next)) {
            error("route offset override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((sequence.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetOverride {
        return RouteOffsetOverride(
            code = code,
            title = title,
            expiresAt = expiresAt,
            margin = margin,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset override in status open. */
fun newRouteOffsetOverride(code: String, title: String): RouteOffsetOverride {
    return RouteOffsetOverride(
        code = code,
        title = title,
        expiresAt = 0,
        margin = 0.0,
        sequence = 0,
        status = RouteOffsetOverrideStatus.OPEN,
    )
}
