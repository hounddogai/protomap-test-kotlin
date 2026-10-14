package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize

/** Where a redelivery forecast is in its life cycle. */
enum class RedeliveryForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One redelivery forecast, with what the planners need to act on it. */
data class RedeliveryForecast(
    var code: String,
    var title: String,
    var version: Int,
    var country: String,
    var routeCode: String,
    var status: RedeliveryForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryForecastStatus): Boolean {
        when (status) {
            RedeliveryForecastStatus.PLANNED -> {
                when (next) {
                    RedeliveryForecastStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryForecastStatus.SCHEDULED -> return next == RedeliveryForecastStatus.IN_PROGRESS || next == RedeliveryForecastStatus.CANCELLED
            RedeliveryForecastStatus.IN_PROGRESS -> return next == RedeliveryForecastStatus.COMPLETED
            RedeliveryForecastStatus.COMPLETED -> return next == RedeliveryForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RedeliveryForecastStatus) {
        if (!canMoveTo(next)) {
            error("redelivery forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): RedeliveryForecast {
        return RedeliveryForecast(
            code = code,
            title = title,
            version = version,
            country = country,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a redelivery forecast in status planned. */
fun newRedeliveryForecast(code: String, title: String): RedeliveryForecast {
    return RedeliveryForecast(
        code = code,
        title = title,
        version = 0,
        country = "",
        routeCode = "",
        status = RedeliveryForecastStatus.PLANNED,
    )
}
