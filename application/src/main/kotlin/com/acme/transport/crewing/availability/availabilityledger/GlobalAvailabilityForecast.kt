package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global availability forecast is in its life cycle. */
enum class GlobalAvailabilityForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global availability forecast, with what the planners need to act on it. */
data class GlobalAvailabilityForecast(
    var code: String,
    var label: String,
    var discount: Double,
    var limit: Int,
    var capacity: Int,
    var attempts: Int,
    var status: GlobalAvailabilityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAvailabilityForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAvailabilityForecastStatus): Boolean {
        when (status) {
            GlobalAvailabilityForecastStatus.CREATED -> {
                when (next) {
                    GlobalAvailabilityForecastStatus.VALIDATED, GlobalAvailabilityForecastStatus.RETIRED -> return true
                }
                return false
            }
            GlobalAvailabilityForecastStatus.VALIDATED -> return next == GlobalAvailabilityForecastStatus.PUBLISHED || next == GlobalAvailabilityForecastStatus.RETIRED
            GlobalAvailabilityForecastStatus.PUBLISHED -> return next == GlobalAvailabilityForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalAvailabilityForecastStatus) {
        if (!canMoveTo(next)) {
            error("global availability forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round(limit.toDouble() * discount * 100) / 100

    fun snapshot(): GlobalAvailabilityForecast {
        return GlobalAvailabilityForecast(
            code = code,
            label = label,
            discount = discount,
            limit = limit,
            capacity = capacity,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global availability forecast in status created. */
fun newGlobalAvailabilityForecast(code: String, label: String): GlobalAvailabilityForecast {
    return GlobalAvailabilityForecast(
        code = code,
        label = label,
        discount = 0.0,
        limit = 0,
        capacity = 0,
        attempts = 0,
        status = GlobalAvailabilityForecastStatus.CREATED,
    )
}
