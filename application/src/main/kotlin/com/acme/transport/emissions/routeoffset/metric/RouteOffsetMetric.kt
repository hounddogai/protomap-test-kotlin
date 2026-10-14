package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset metric records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset metric is in its life cycle. */
enum class RouteOffsetMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One route offset metric, with what the planners need to act on it. */
data class RouteOffsetMetric(
    var code: String,
    var name: String,
    var lane: String,
    var distanceKm: Double,
    var priority: Int,
    var status: RouteOffsetMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetMetricStatus): Boolean {
        when (status) {
            RouteOffsetMetricStatus.NEW -> return next == RouteOffsetMetricStatus.QUEUED || next == RouteOffsetMetricStatus.DONE
            RouteOffsetMetricStatus.QUEUED -> return next == RouteOffsetMetricStatus.RUNNING || next == RouteOffsetMetricStatus.FAILED
            RouteOffsetMetricStatus.RUNNING -> {
                when (next) {
                    RouteOffsetMetricStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetMetricStatus.DONE -> return next == RouteOffsetMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RouteOffsetMetricStatus) {
        if (!canMoveTo(next)) {
            error("route offset metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((distanceKm - priority.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetMetric {
        return RouteOffsetMetric(
            code = code,
            name = name,
            lane = lane,
            distanceKm = distanceKm,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a route offset metric in status new. */
fun newRouteOffsetMetric(code: String, name: String): RouteOffsetMetric {
    return RouteOffsetMetric(
        code = code,
        name = name,
        lane = "",
        distanceKm = 0.0,
        priority = 0,
        status = RouteOffsetMetricStatus.NEW,
    )
}
