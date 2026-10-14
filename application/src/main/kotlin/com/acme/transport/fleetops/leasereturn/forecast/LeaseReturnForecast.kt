package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return forecast records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return forecast is in its life cycle. */
enum class LeaseReturnForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lease return forecast, with what the planners need to act on it. */
data class LeaseReturnForecast(
    var code: String,
    var title: String,
    var shiftCode: String,
    var threshold: Double,
    var stopCount: Int,
    var createdAt: Int,
    var loadKg: Double,
    var retries: Int,
    var maximum: Double,
    var odometer: Int,
    var status: LeaseReturnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnForecastStatus): Boolean {
        when (status) {
            LeaseReturnForecastStatus.PROPOSED -> {
                when (next) {
                    LeaseReturnForecastStatus.REJECTED, LeaseReturnForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            LeaseReturnForecastStatus.APPROVED -> return next == LeaseReturnForecastStatus.REJECTED || next == LeaseReturnForecastStatus.WITHDRAWN
            LeaseReturnForecastStatus.REJECTED -> return next == LeaseReturnForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LeaseReturnForecastStatus) {
        if (!canMoveTo(next)) {
            error("lease return forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round(odometer.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnForecast {
        return LeaseReturnForecast(
            code = code,
            title = title,
            shiftCode = shiftCode,
            threshold = threshold,
            stopCount = stopCount,
            createdAt = createdAt,
            loadKg = loadKg,
            retries = retries,
            maximum = maximum,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lease return forecast in status proposed. */
fun newLeaseReturnForecast(code: String, title: String): LeaseReturnForecast {
    return LeaseReturnForecast(
        code = code,
        title = title,
        shiftCode = "",
        threshold = 0.0,
        stopCount = 0,
        createdAt = 0,
        loadKg = 0.0,
        retries = 0,
        maximum = 0.0,
        odometer = 0,
        status = LeaseReturnForecastStatus.PROPOSED,
    )
}
