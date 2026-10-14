package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling duty period forecast is in its life cycle. */
enum class RollingDutyPeriodForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling duty period forecast, with what the planners need to act on it. */
data class RollingDutyPeriodForecast(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var odometer: Int,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var driverId: String,
    var name: String,
    var contactEmail: String,
    var stopCount: Int,
    var status: RollingDutyPeriodForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodForecastStatus): Boolean {
        when (status) {
            RollingDutyPeriodForecastStatus.PROPOSED -> return next == RollingDutyPeriodForecastStatus.APPROVED
            RollingDutyPeriodForecastStatus.APPROVED -> return next == RollingDutyPeriodForecastStatus.WITHDRAWN
            RollingDutyPeriodForecastStatus.REJECTED -> return next == RollingDutyPeriodForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodForecastStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((budget + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingDutyPeriodForecast {
        return RollingDutyPeriodForecast(
            code = code,
            label = label,
            pinned = pinned,
            odometer = odometer,
            windowDays = windowDays,
            attributes = attributes,
            budget = budget,
            driverId = driverId,
            name = name,
            contactEmail = contactEmail,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling duty period forecast in status proposed. */
fun newRollingDutyPeriodForecast(code: String, label: String): RollingDutyPeriodForecast {
    return RollingDutyPeriodForecast(
        code = code,
        label = label,
        pinned = false,
        odometer = 0,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        driverId = "",
        name = "",
        contactEmail = "",
        stopCount = 0,
        status = RollingDutyPeriodForecastStatus.PROPOSED,
    )
}
