package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset forecast records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset forecast is in its life cycle. */
enum class RouteOffsetForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One route offset forecast, with what the planners need to act on it. */
data class RouteOffsetForecast(
    var code: String,
    var name: String,
    var ownerId: String,
    var durationMinutes: Int,
    var ratio: Double,
    var status: RouteOffsetForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetForecastStatus): Boolean {
        when (status) {
            RouteOffsetForecastStatus.PLANNED -> {
                when (next) {
                    RouteOffsetForecastStatus.IN_PROGRESS, RouteOffsetForecastStatus.COMPLETED -> return true
                }
                return false
            }
            RouteOffsetForecastStatus.SCHEDULED -> return next == RouteOffsetForecastStatus.IN_PROGRESS || next == RouteOffsetForecastStatus.CANCELLED
            RouteOffsetForecastStatus.IN_PROGRESS -> return next == RouteOffsetForecastStatus.COMPLETED || next == RouteOffsetForecastStatus.CANCELLED
            RouteOffsetForecastStatus.COMPLETED -> return next == RouteOffsetForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RouteOffsetForecastStatus) {
        if (!canMoveTo(next)) {
            error("route offset forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"route offset forecast"} $code"

    fun weightedVolume(): Double = round(ratio * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetForecast {
        return RouteOffsetForecast(
            code = code,
            name = name,
            ownerId = ownerId,
            durationMinutes = durationMinutes,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a route offset forecast in status planned. */
fun newRouteOffsetForecast(code: String, name: String): RouteOffsetForecast {
    return RouteOffsetForecast(
        code = code,
        name = name,
        ownerId = "",
        durationMinutes = 0,
        ratio = 0.0,
        status = RouteOffsetForecastStatus.PLANNED,
    )
}
