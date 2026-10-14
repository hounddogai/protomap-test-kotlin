package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional trailer yard forecast records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a regional trailer yard forecast is in its life cycle. */
enum class RegionalTrailerYardForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional trailer yard forecast and its current status. */
data class RegionalTrailerYardForecast(
    var code: String,
    var title: String,
    var ceiling: Double,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var pinned: Boolean,
    var weight: Double,
    var category: String,
    var description: String,
    var source: String,
    var status: RegionalTrailerYardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardForecastStatus): Boolean {
        when (status) {
            RegionalTrailerYardForecastStatus.REQUESTED -> {
                when (next) {
                    RegionalTrailerYardForecastStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalTrailerYardForecastStatus.GRANTED -> {
                when (next) {
                    RegionalTrailerYardForecastStatus.REVOKED -> return true
                }
                return false
            }
            RegionalTrailerYardForecastStatus.REVOKED -> return next == RegionalTrailerYardForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardForecastStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional trailer yard forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((threshold - ceiling) * 100) / 100

    fun snapshot(): RegionalTrailerYardForecast {
        return RegionalTrailerYardForecast(
            code = code,
            title = title,
            ceiling = ceiling,
            attributes = attributes,
            threshold = threshold,
            pinned = pinned,
            weight = weight,
            category = category,
            description = description,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional trailer yard forecast in status requested. */
fun newRegionalTrailerYardForecast(code: String, title: String): RegionalTrailerYardForecast {
    return RegionalTrailerYardForecast(
        code = code,
        title = title,
        ceiling = 0.0,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        pinned = false,
        weight = 0.0,
        category = "",
        description = "",
        source = "",
        status = RegionalTrailerYardForecastStatus.REQUESTED,
    )
}
