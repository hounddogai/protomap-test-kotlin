package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender forecast is in its life cycle. */
enum class GlobalTenderForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global tender forecast, with what the planners need to act on it. */
data class GlobalTenderForecast(
    var code: String,
    var label: String,
    var dock: String,
    var odometer: Int,
    var retries: Int,
    var shiftCode: String,
    var score: Double,
    var status: GlobalTenderForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderForecastStatus): Boolean {
        when (status) {
            GlobalTenderForecastStatus.PLANNED -> return next == GlobalTenderForecastStatus.IN_PROGRESS || next == GlobalTenderForecastStatus.CANCELLED
            GlobalTenderForecastStatus.SCHEDULED -> {
                when (next) {
                    GlobalTenderForecastStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalTenderForecastStatus.IN_PROGRESS -> return next == GlobalTenderForecastStatus.COMPLETED || next == GlobalTenderForecastStatus.CANCELLED
            GlobalTenderForecastStatus.COMPLETED -> return next == GlobalTenderForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalTenderForecastStatus) {
        if (!canMoveTo(next)) {
            error("global tender forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((odometer.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalTenderForecast {
        return GlobalTenderForecast(
            code = code,
            label = label,
            dock = dock,
            odometer = odometer,
            retries = retries,
            shiftCode = shiftCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global tender forecast in status planned. */
fun newGlobalTenderForecast(code: String, label: String): GlobalTenderForecast {
    return GlobalTenderForecast(
        code = code,
        label = label,
        dock = "",
        odometer = 0,
        retries = 0,
        shiftCode = "",
        score = 0.0,
        status = GlobalTenderForecastStatus.PLANNED,
    )
}
