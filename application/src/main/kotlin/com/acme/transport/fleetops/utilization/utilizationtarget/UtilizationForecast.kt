package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization forecast is in its life cycle. */
enum class UtilizationForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One utilization forecast, with what the planners need to act on it. */
data class UtilizationForecast(
    var code: String,
    var label: String,
    var endsAt: Int,
    var version: Int,
    var budget: Double,
    var status: UtilizationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationForecastStatus): Boolean {
        when (status) {
            UtilizationForecastStatus.REQUESTED -> {
                when (next) {
                    UtilizationForecastStatus.EXPIRED -> return true
                }
                return false
            }
            UtilizationForecastStatus.GRANTED -> return next == UtilizationForecastStatus.EXPIRED
            UtilizationForecastStatus.REVOKED -> {
                when (next) {
                    UtilizationForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationForecastStatus) {
        if (!canMoveTo(next)) {
            error("utilization forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round(version.toDouble() * budget * 100) / 100

    fun snapshot(): UtilizationForecast {
        return UtilizationForecast(
            code = code,
            label = label,
            endsAt = endsAt,
            version = version,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization forecast in status requested. */
fun newUtilizationForecast(code: String, label: String): UtilizationForecast {
    return UtilizationForecast(
        code = code,
        label = label,
        endsAt = 0,
        version = 0,
        budget = 0.0,
        status = UtilizationForecastStatus.REQUESTED,
    )
}
