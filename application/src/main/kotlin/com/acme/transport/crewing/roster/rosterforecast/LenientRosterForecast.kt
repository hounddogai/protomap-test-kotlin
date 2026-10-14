package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient roster forecast records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a lenient roster forecast is in its life cycle. */
enum class LenientRosterForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient roster forecast, with what the planners need to act on it. */
data class LenientRosterForecast(
    var code: String,
    var name: String,
    var pallets: Int,
    var distanceKm: Double,
    var driverId: String,
    var maximum: Double,
    var locale: String,
    var status: LenientRosterForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRosterForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRosterForecastStatus): Boolean {
        when (status) {
            LenientRosterForecastStatus.IDLE -> return next == LenientRosterForecastStatus.READY
            LenientRosterForecastStatus.WARMING -> return next == LenientRosterForecastStatus.READY
            LenientRosterForecastStatus.READY -> return next == LenientRosterForecastStatus.DRAINING
            LenientRosterForecastStatus.DRAINING -> {
                when (next) {
                    LenientRosterForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRosterForecastStatus) {
        if (!canMoveTo(next)) {
            error("lenient roster forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round(pallets.toDouble() * distanceKm * 100) / 100

    fun snapshot(): LenientRosterForecast {
        return LenientRosterForecast(
            code = code,
            name = name,
            pallets = pallets,
            distanceKm = distanceKm,
            driverId = driverId,
            maximum = maximum,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient roster forecast in status idle. */
fun newLenientRosterForecast(code: String, name: String): LenientRosterForecast {
    return LenientRosterForecast(
        code = code,
        name = name,
        pallets = 0,
        distanceKm = 0.0,
        driverId = "",
        maximum = 0.0,
        locale = "",
        status = LenientRosterForecastStatus.IDLE,
    )
}
