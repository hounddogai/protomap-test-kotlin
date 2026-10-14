package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual lease return forecast is in its life cycle. */
enum class ManualLeaseReturnForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual lease return forecast, with what the planners need to act on it. */
data class ManualLeaseReturnForecast(
    var code: String,
    var label: String,
    var attempts: Int,
    var timezone: String,
    var threshold: Double,
    var ratio: Double,
    var etaMinutes: Int,
    var margin: Double,
    var status: ManualLeaseReturnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLeaseReturnForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLeaseReturnForecastStatus): Boolean {
        when (status) {
            ManualLeaseReturnForecastStatus.CREATED -> {
                when (next) {
                    ManualLeaseReturnForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualLeaseReturnForecastStatus.VALIDATED -> {
                when (next) {
                    ManualLeaseReturnForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualLeaseReturnForecastStatus.PUBLISHED -> {
                when (next) {
                    ManualLeaseReturnForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualLeaseReturnForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual lease return forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((etaMinutes.toDouble() - ratio) * 100) / 100

    fun snapshot(): ManualLeaseReturnForecast {
        return ManualLeaseReturnForecast(
            code = code,
            label = label,
            attempts = attempts,
            timezone = timezone,
            threshold = threshold,
            ratio = ratio,
            etaMinutes = etaMinutes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual lease return forecast in status created. */
fun newManualLeaseReturnForecast(code: String, label: String): ManualLeaseReturnForecast {
    return ManualLeaseReturnForecast(
        code = code,
        label = label,
        attempts = 0,
        timezone = "",
        threshold = 0.0,
        ratio = 0.0,
        etaMinutes = 0,
        margin = 0.0,
        status = ManualLeaseReturnForecastStatus.CREATED,
    )
}
