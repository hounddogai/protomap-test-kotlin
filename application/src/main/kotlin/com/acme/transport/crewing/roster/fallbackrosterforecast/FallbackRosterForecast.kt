package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback roster forecast records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a fallback roster forecast is in its life cycle. */
enum class FallbackRosterForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback roster forecast. Its code is unique within a tenant. */
data class FallbackRosterForecast(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var quantity: Int,
    var reviewer: String,
    var routeCode: String,
    var status: FallbackRosterForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRosterForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRosterForecastStatus): Boolean {
        when (status) {
            FallbackRosterForecastStatus.PLANNED -> return next == FallbackRosterForecastStatus.SCHEDULED
            FallbackRosterForecastStatus.SCHEDULED -> return next == FallbackRosterForecastStatus.COMPLETED
            FallbackRosterForecastStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackRosterForecastStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackRosterForecastStatus.COMPLETED -> return next == FallbackRosterForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackRosterForecastStatus) {
        if (!canMoveTo(next)) {
            error("fallback roster forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback roster forecast"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((quantity.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): FallbackRosterForecast {
        return FallbackRosterForecast(
            code = code,
            title = title,
            tags = tags,
            fuelLiters = fuelLiters,
            quantity = quantity,
            reviewer = reviewer,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback roster forecast in status planned. */
fun newFallbackRosterForecast(code: String, title: String): FallbackRosterForecast {
    return FallbackRosterForecast(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        quantity = 0,
        reviewer = "",
        routeCode = "",
        status = FallbackRosterForecastStatus.PLANNED,
    )
}
