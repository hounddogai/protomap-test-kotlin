package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual carbon report forecast records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a manual carbon report forecast is in its life cycle. */
enum class ManualCarbonReportForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual carbon report forecast, with what the planners need to act on it. */
data class ManualCarbonReportForecast(
    var code: String,
    var label: String,
    var pallets: Int,
    var locale: String,
    var floorValue: Double,
    var createdAt: Int,
    var status: ManualCarbonReportForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCarbonReportForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCarbonReportForecastStatus): Boolean {
        when (status) {
            ManualCarbonReportForecastStatus.DRAFT -> return next == ManualCarbonReportForecastStatus.PENDING
            ManualCarbonReportForecastStatus.PENDING -> return next == ManualCarbonReportForecastStatus.ACTIVE || next == ManualCarbonReportForecastStatus.CLOSED
            ManualCarbonReportForecastStatus.ACTIVE -> return next == ManualCarbonReportForecastStatus.SUSPENDED
            ManualCarbonReportForecastStatus.SUSPENDED -> {
                when (next) {
                    ManualCarbonReportForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCarbonReportForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual carbon report forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(createdAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): ManualCarbonReportForecast {
        return ManualCarbonReportForecast(
            code = code,
            label = label,
            pallets = pallets,
            locale = locale,
            floorValue = floorValue,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual carbon report forecast in status draft. */
fun newManualCarbonReportForecast(code: String, label: String): ManualCarbonReportForecast {
    return ManualCarbonReportForecast(
        code = code,
        label = label,
        pallets = 0,
        locale = "",
        floorValue = 0.0,
        createdAt = 0,
        status = ManualCarbonReportForecastStatus.DRAFT,
    )
}
