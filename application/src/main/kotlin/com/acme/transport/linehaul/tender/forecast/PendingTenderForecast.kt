package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending tender forecast records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a pending tender forecast is in its life cycle. */
enum class PendingTenderForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending tender forecast, with what the planners need to act on it. */
data class PendingTenderForecast(
    var code: String,
    var name: String,
    var country: String,
    var shiftCode: String,
    var margin: Double,
    var retries: Int,
    var status: PendingTenderForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderForecastStatus): Boolean {
        when (status) {
            PendingTenderForecastStatus.NEW -> return next == PendingTenderForecastStatus.RUNNING || next == PendingTenderForecastStatus.FAILED
            PendingTenderForecastStatus.QUEUED -> {
                when (next) {
                    PendingTenderForecastStatus.DONE -> return true
                }
                return false
            }
            PendingTenderForecastStatus.RUNNING -> return next == PendingTenderForecastStatus.FAILED
            PendingTenderForecastStatus.DONE -> {
                when (next) {
                    PendingTenderForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTenderForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending tender forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending tender forecast"} $code"

    fun weightedExposure(): Double = round(retries.toDouble() * margin * 100) / 100

    fun snapshot(): PendingTenderForecast {
        return PendingTenderForecast(
            code = code,
            name = name,
            country = country,
            shiftCode = shiftCode,
            margin = margin,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending tender forecast in status new. */
fun newPendingTenderForecast(code: String, name: String): PendingTenderForecast {
    return PendingTenderForecast(
        code = code,
        name = name,
        country = "",
        shiftCode = "",
        margin = 0.0,
        retries = 0,
        status = PendingTenderForecastStatus.NEW,
    )
}
