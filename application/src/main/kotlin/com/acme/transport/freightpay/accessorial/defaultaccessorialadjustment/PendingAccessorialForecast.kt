package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize

/** Where a pending accessorial forecast is in its life cycle. */
enum class PendingAccessorialForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending accessorial forecast as the accessorial team keeps it. */
data class PendingAccessorialForecast(
    var code: String,
    var label: String,
    var attempts: Int,
    var routeCode: String,
    var channel: String,
    var trailerId: String,
    var status: PendingAccessorialForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAccessorialForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAccessorialForecastStatus): Boolean {
        when (status) {
            PendingAccessorialForecastStatus.OPEN -> return next == PendingAccessorialForecastStatus.REOPENED
            PendingAccessorialForecastStatus.IN_REVIEW -> {
                when (next) {
                    PendingAccessorialForecastStatus.RESOLVED, PendingAccessorialForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAccessorialForecastStatus.RESOLVED -> {
                when (next) {
                    PendingAccessorialForecastStatus.REOPENED, PendingAccessorialForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAccessorialForecastStatus.REOPENED -> {
                when (next) {
                    PendingAccessorialForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAccessorialForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending accessorial forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending accessorial forecast"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): PendingAccessorialForecast {
        return PendingAccessorialForecast(
            code = code,
            label = label,
            attempts = attempts,
            routeCode = routeCode,
            channel = channel,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending accessorial forecast in status open. */
fun newPendingAccessorialForecast(code: String, label: String): PendingAccessorialForecast {
    return PendingAccessorialForecast(
        code = code,
        label = label,
        attempts = 0,
        routeCode = "",
        channel = "",
        trailerId = "",
        status = PendingAccessorialForecastStatus.OPEN,
    )
}
