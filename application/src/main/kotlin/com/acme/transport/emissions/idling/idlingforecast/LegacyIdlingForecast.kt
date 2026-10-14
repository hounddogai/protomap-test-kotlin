package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy idling forecast records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a legacy idling forecast is in its life cycle. */
enum class LegacyIdlingForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy idling forecast, with what the planners need to act on it. */
data class LegacyIdlingForecast(
    var code: String,
    var title: String,
    var vehicleId: String,
    var endsAt: Int,
    var source: String,
    var limit: Int,
    var sequence: Int,
    var status: LegacyIdlingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIdlingForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIdlingForecastStatus): Boolean {
        when (status) {
            LegacyIdlingForecastStatus.OPEN -> {
                when (next) {
                    LegacyIdlingForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyIdlingForecastStatus.IN_REVIEW -> return next == LegacyIdlingForecastStatus.RESOLVED || next == LegacyIdlingForecastStatus.REOPENED
            LegacyIdlingForecastStatus.RESOLVED -> {
                when (next) {
                    LegacyIdlingForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyIdlingForecastStatus.REOPENED -> {
                when (next) {
                    LegacyIdlingForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIdlingForecastStatus) {
        if (!canMoveTo(next)) {
            error("legacy idling forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((sequence.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyIdlingForecast {
        return LegacyIdlingForecast(
            code = code,
            title = title,
            vehicleId = vehicleId,
            endsAt = endsAt,
            source = source,
            limit = limit,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy idling forecast in status open. */
fun newLegacyIdlingForecast(code: String, title: String): LegacyIdlingForecast {
    return LegacyIdlingForecast(
        code = code,
        title = title,
        vehicleId = "",
        endsAt = 0,
        source = "",
        limit = 0,
        sequence = 0,
        status = LegacyIdlingForecastStatus.OPEN,
    )
}
