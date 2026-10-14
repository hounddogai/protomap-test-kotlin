package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break forecast is in its life cycle. */
enum class RestBreakForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rest break forecast as the rest break team keeps it. */
data class RestBreakForecast(
    var code: String,
    var title: String,
    var budget: Double,
    var attempts: Int,
    var margin: Double,
    var label: String,
    var weight: Double,
    var active: Boolean,
    var currency: String,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var status: RestBreakForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakForecastStatus): Boolean {
        when (status) {
            RestBreakForecastStatus.PROPOSED -> {
                when (next) {
                    RestBreakForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakForecastStatus.APPROVED -> {
                when (next) {
                    RestBreakForecastStatus.REJECTED -> return true
                }
                return false
            }
            RestBreakForecastStatus.REJECTED -> {
                when (next) {
                    RestBreakForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakForecastStatus) {
        if (!canMoveTo(next)) {
            error("rest break forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((distanceKm + weight) * 100) / 100

    fun snapshot(): RestBreakForecast {
        return RestBreakForecast(
            code = code,
            title = title,
            budget = budget,
            attempts = attempts,
            margin = margin,
            label = label,
            weight = weight,
            active = active,
            currency = currency,
            tags = tags,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rest break forecast in status proposed. */
fun newRestBreakForecast(code: String, title: String): RestBreakForecast {
    return RestBreakForecast(
        code = code,
        title = title,
        budget = 0.0,
        attempts = 0,
        margin = 0.0,
        label = "",
        weight = 0.0,
        active = false,
        currency = "",
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        status = RestBreakForecastStatus.PROPOSED,
    )
}
