package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted broker forecast records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a weighted broker forecast is in its life cycle. */
enum class WeightedBrokerForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted broker forecast as the broker team keeps it. */
data class WeightedBrokerForecast(
    var code: String,
    var label: String,
    var windowDays: Int,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var currency: String,
    var score: Double,
    var notes: String,
    var dock: String,
    var minimum: Double,
    var status: WeightedBrokerForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBrokerForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBrokerForecastStatus): Boolean {
        when (status) {
            WeightedBrokerForecastStatus.DRAFT -> {
                when (next) {
                    WeightedBrokerForecastStatus.CLOSED -> return true
                }
                return false
            }
            WeightedBrokerForecastStatus.PENDING -> return next == WeightedBrokerForecastStatus.ACTIVE
            WeightedBrokerForecastStatus.ACTIVE -> {
                when (next) {
                    WeightedBrokerForecastStatus.SUSPENDED, WeightedBrokerForecastStatus.CLOSED -> return true
                }
                return false
            }
            WeightedBrokerForecastStatus.SUSPENDED -> return next == WeightedBrokerForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedBrokerForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted broker forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted broker forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((minimum + windowDays.toDouble()) * 100) / 100

    fun snapshot(): WeightedBrokerForecast {
        return WeightedBrokerForecast(
            code = code,
            label = label,
            windowDays = windowDays,
            unit = unit,
            attributes = attributes,
            currency = currency,
            score = score,
            notes = notes,
            dock = dock,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted broker forecast in status draft. */
fun newWeightedBrokerForecast(code: String, label: String): WeightedBrokerForecast {
    return WeightedBrokerForecast(
        code = code,
        label = label,
        windowDays = 0,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        currency = "",
        score = 0.0,
        notes = "",
        dock = "",
        minimum = 0.0,
        status = WeightedBrokerForecastStatus.DRAFT,
    )
}
