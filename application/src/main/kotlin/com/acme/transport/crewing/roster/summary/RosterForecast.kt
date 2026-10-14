package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize

/** Where a roster forecast is in its life cycle. */
enum class RosterForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One roster forecast, with what the planners need to act on it. */
data class RosterForecast(
    var code: String,
    var label: String,
    var lane: String,
    var priority: Int,
    var locale: String,
    var driverId: String,
    var status: RosterForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterForecastStatus): Boolean {
        when (status) {
            RosterForecastStatus.PLANNED -> return next == RosterForecastStatus.SCHEDULED
            RosterForecastStatus.SCHEDULED -> return next == RosterForecastStatus.CANCELLED
            RosterForecastStatus.IN_PROGRESS -> return next == RosterForecastStatus.COMPLETED || next == RosterForecastStatus.CANCELLED
            RosterForecastStatus.COMPLETED -> {
                when (next) {
                    RosterForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterForecastStatus) {
        if (!canMoveTo(next)) {
            error("roster forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster forecast"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): RosterForecast {
        return RosterForecast(
            code = code,
            label = label,
            lane = lane,
            priority = priority,
            locale = locale,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a roster forecast in status planned. */
fun newRosterForecast(code: String, label: String): RosterForecast {
    return RosterForecast(
        code = code,
        label = label,
        lane = "",
        priority = 0,
        locale = "",
        driverId = "",
        status = RosterForecastStatus.PLANNED,
    )
}
