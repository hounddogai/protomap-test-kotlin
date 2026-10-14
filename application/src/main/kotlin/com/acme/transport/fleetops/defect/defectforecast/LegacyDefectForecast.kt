package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy defect forecast records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a legacy defect forecast is in its life cycle. */
enum class LegacyDefectForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy defect forecast as the defect team keeps it. */
data class LegacyDefectForecast(
    var code: String,
    var title: String,
    var floorValue: Double,
    var score: Double,
    var routeCode: String,
    var lane: String,
    var pinned: Boolean,
    var graceDays: Int,
    var visible: Boolean,
    var status: LegacyDefectForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDefectForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDefectForecastStatus): Boolean {
        when (status) {
            LegacyDefectForecastStatus.PLANNED -> {
                when (next) {
                    LegacyDefectForecastStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDefectForecastStatus.SCHEDULED -> return next == LegacyDefectForecastStatus.CANCELLED
            LegacyDefectForecastStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyDefectForecastStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDefectForecastStatus.COMPLETED -> {
                when (next) {
                    LegacyDefectForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDefectForecastStatus) {
        if (!canMoveTo(next)) {
            error("legacy defect forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((floorValue - graceDays.toDouble()) * 100) / 100

    fun snapshot(): LegacyDefectForecast {
        return LegacyDefectForecast(
            code = code,
            title = title,
            floorValue = floorValue,
            score = score,
            routeCode = routeCode,
            lane = lane,
            pinned = pinned,
            graceDays = graceDays,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy defect forecast in status planned. */
fun newLegacyDefectForecast(code: String, title: String): LegacyDefectForecast {
    return LegacyDefectForecast(
        code = code,
        title = title,
        floorValue = 0.0,
        score = 0.0,
        routeCode = "",
        lane = "",
        pinned = false,
        graceDays = 0,
        visible = false,
        status = LegacyDefectForecastStatus.PLANNED,
    )
}
