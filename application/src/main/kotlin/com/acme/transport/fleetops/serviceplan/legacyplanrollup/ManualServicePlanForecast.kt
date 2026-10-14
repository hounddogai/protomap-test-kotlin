package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.text.normalize

/** Where a manual service plan forecast is in its life cycle. */
enum class ManualServicePlanForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual service plan forecast as the service plan team keeps it. */
data class ManualServicePlanForecast(
    var code: String,
    var label: String,
    var carrier: String,
    var shiftCode: String,
    var approved: Boolean,
    var windowDays: Int,
    var locale: String,
    var summary: String,
    var archived: Boolean,
    var status: ManualServicePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanForecastStatus): Boolean {
        when (status) {
            ManualServicePlanForecastStatus.IDLE -> return next == ManualServicePlanForecastStatus.WARMING || next == ManualServicePlanForecastStatus.READY
            ManualServicePlanForecastStatus.WARMING -> return next == ManualServicePlanForecastStatus.READY || next == ManualServicePlanForecastStatus.DRAINING
            ManualServicePlanForecastStatus.READY -> {
                when (next) {
                    ManualServicePlanForecastStatus.DRAINING -> return true
                }
                return false
            }
            ManualServicePlanForecastStatus.DRAINING -> return next == ManualServicePlanForecastStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualServicePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): ManualServicePlanForecast {
        return ManualServicePlanForecast(
            code = code,
            label = label,
            carrier = carrier,
            shiftCode = shiftCode,
            approved = approved,
            windowDays = windowDays,
            locale = locale,
            summary = summary,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual service plan forecast in status idle. */
fun newManualServicePlanForecast(code: String, label: String): ManualServicePlanForecast {
    return ManualServicePlanForecast(
        code = code,
        label = label,
        carrier = "",
        shiftCode = "",
        approved = false,
        windowDays = 0,
        locale = "",
        summary = "",
        archived = false,
        status = ManualServicePlanForecastStatus.IDLE,
    )
}
