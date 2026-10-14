package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset adjustment is in its life cycle. */
enum class RouteOffsetAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A route offset adjustment as the route offset team keeps it. */
data class RouteOffsetAdjustment(
    var code: String,
    var title: String,
    var name: String,
    var startsAt: Int,
    var score: Double,
    var status: RouteOffsetAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetAdjustmentStatus): Boolean {
        when (status) {
            RouteOffsetAdjustmentStatus.NEW -> return next == RouteOffsetAdjustmentStatus.DONE
            RouteOffsetAdjustmentStatus.QUEUED -> return next == RouteOffsetAdjustmentStatus.RUNNING || next == RouteOffsetAdjustmentStatus.DONE
            RouteOffsetAdjustmentStatus.RUNNING -> {
                when (next) {
                    RouteOffsetAdjustmentStatus.DONE -> return true
                }
                return false
            }
            RouteOffsetAdjustmentStatus.DONE -> {
                when (next) {
                    RouteOffsetAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("route offset adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset adjustment"} $code"

    fun effectiveValue(): Double = round((score - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetAdjustment {
        return RouteOffsetAdjustment(
            code = code,
            title = title,
            name = name,
            startsAt = startsAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset adjustment in status new. */
fun newRouteOffsetAdjustment(code: String, title: String): RouteOffsetAdjustment {
    return RouteOffsetAdjustment(
        code = code,
        title = title,
        name = "",
        startsAt = 0,
        score = 0.0,
        status = RouteOffsetAdjustmentStatus.NEW,
    )
}
