package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.normalize

/** Where a scheduled defect forecast is in its life cycle. */
enum class ScheduledDefectForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled defect forecast, with what the planners need to act on it. */
data class ScheduledDefectForecast(
    var code: String,
    var label: String,
    var lane: String,
    var ownerId: String,
    var retries: Int,
    var name: String,
    var carrier: String,
    var routeCode: String,
    var status: ScheduledDefectForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectForecastStatus): Boolean {
        when (status) {
            ScheduledDefectForecastStatus.DRAFT -> return next == ScheduledDefectForecastStatus.ACTIVE || next == ScheduledDefectForecastStatus.CLOSED
            ScheduledDefectForecastStatus.PENDING -> {
                when (next) {
                    ScheduledDefectForecastStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDefectForecastStatus.ACTIVE -> {
                when (next) {
                    ScheduledDefectForecastStatus.SUSPENDED, ScheduledDefectForecastStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledDefectForecastStatus.SUSPENDED -> {
                when (next) {
                    ScheduledDefectForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDefectForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled defect forecast"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): ScheduledDefectForecast {
        return ScheduledDefectForecast(
            code = code,
            label = label,
            lane = lane,
            ownerId = ownerId,
            retries = retries,
            name = name,
            carrier = carrier,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled defect forecast in status draft. */
fun newScheduledDefectForecast(code: String, label: String): ScheduledDefectForecast {
    return ScheduledDefectForecast(
        code = code,
        label = label,
        lane = "",
        ownerId = "",
        retries = 0,
        name = "",
        carrier = "",
        routeCode = "",
        status = ScheduledDefectForecastStatus.DRAFT,
    )
}
