package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dwell forecast is in its life cycle. */
enum class ManualDwellForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual dwell forecast as the dwell team keeps it. */
data class ManualDwellForecast(
    var code: String,
    var title: String,
    var trailerId: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var region: String,
    var maximum: Double,
    var stopCount: Int,
    var version: Int,
    var loadKg: Double,
    var depot: String,
    var status: ManualDwellForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellForecastStatus): Boolean {
        when (status) {
            ManualDwellForecastStatus.CREATED -> {
                when (next) {
                    ManualDwellForecastStatus.VALIDATED, ManualDwellForecastStatus.RETIRED -> return true
                }
                return false
            }
            ManualDwellForecastStatus.VALIDATED -> {
                when (next) {
                    ManualDwellForecastStatus.PUBLISHED, ManualDwellForecastStatus.RETIRED -> return true
                }
                return false
            }
            ManualDwellForecastStatus.PUBLISHED -> return next == ManualDwellForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualDwellForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round(version.toDouble() * loadKg * 100) / 100

    fun snapshot(): ManualDwellForecast {
        return ManualDwellForecast(
            code = code,
            title = title,
            trailerId = trailerId,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            region = region,
            maximum = maximum,
            stopCount = stopCount,
            version = version,
            loadKg = loadKg,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual dwell forecast in status created. */
fun newManualDwellForecast(code: String, title: String): ManualDwellForecast {
    return ManualDwellForecast(
        code = code,
        title = title,
        trailerId = "",
        graceDays = 0,
        fuelLiters = 0.0,
        region = "",
        maximum = 0.0,
        stopCount = 0,
        version = 0,
        loadKg = 0.0,
        depot = "",
        status = ManualDwellForecastStatus.CREATED,
    )
}
