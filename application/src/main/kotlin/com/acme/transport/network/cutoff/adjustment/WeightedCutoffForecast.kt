package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted cutoff forecast is in its life cycle. */
enum class WeightedCutoffForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted cutoff forecast and its current status. */
data class WeightedCutoffForecast(
    var code: String,
    var name: String,
    var timezone: String,
    var contactEmail: String,
    var category: String,
    var attempts: Int,
    var loadKg: Double,
    var graceDays: Int,
    var updatedAt: Int,
    var status: WeightedCutoffForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCutoffForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCutoffForecastStatus): Boolean {
        when (status) {
            WeightedCutoffForecastStatus.PLANNED -> return next == WeightedCutoffForecastStatus.SCHEDULED || next == WeightedCutoffForecastStatus.COMPLETED
            WeightedCutoffForecastStatus.SCHEDULED -> {
                when (next) {
                    WeightedCutoffForecastStatus.IN_PROGRESS, WeightedCutoffForecastStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedCutoffForecastStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedCutoffForecastStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedCutoffForecastStatus.COMPLETED -> {
                when (next) {
                    WeightedCutoffForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCutoffForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted cutoff forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((graceDays.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): WeightedCutoffForecast {
        return WeightedCutoffForecast(
            code = code,
            name = name,
            timezone = timezone,
            contactEmail = contactEmail,
            category = category,
            attempts = attempts,
            loadKg = loadKg,
            graceDays = graceDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted cutoff forecast in status planned. */
fun newWeightedCutoffForecast(code: String, name: String): WeightedCutoffForecast {
    return WeightedCutoffForecast(
        code = code,
        name = name,
        timezone = "",
        contactEmail = "",
        category = "",
        attempts = 0,
        loadKg = 0.0,
        graceDays = 0,
        updatedAt = 0,
        status = WeightedCutoffForecastStatus.PLANNED,
    )
}
