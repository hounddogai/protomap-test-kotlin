package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize

// The dispute forecast records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute forecast is in its life cycle. */
enum class DisputeForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dispute forecast, with what the planners need to act on it. */
data class DisputeForecast(
    var code: String,
    var label: String,
    var trailerId: String,
    var lane: String,
    var createdAt: Int,
    var name: String,
    var status: DisputeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeForecastStatus): Boolean {
        when (status) {
            DisputeForecastStatus.PLANNED -> {
                when (next) {
                    DisputeForecastStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DisputeForecastStatus.SCHEDULED -> {
                when (next) {
                    DisputeForecastStatus.COMPLETED -> return true
                }
                return false
            }
            DisputeForecastStatus.IN_PROGRESS -> {
                when (next) {
                    DisputeForecastStatus.COMPLETED, DisputeForecastStatus.CANCELLED -> return true
                }
                return false
            }
            DisputeForecastStatus.COMPLETED -> return next == DisputeForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DisputeForecastStatus) {
        if (!canMoveTo(next)) {
            error("dispute forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"dispute forecast"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): DisputeForecast {
        return DisputeForecast(
            code = code,
            label = label,
            trailerId = trailerId,
            lane = lane,
            createdAt = createdAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dispute forecast in status planned. */
fun newDisputeForecast(code: String, label: String): DisputeForecast {
    return DisputeForecast(
        code = code,
        label = label,
        trailerId = "",
        lane = "",
        createdAt = 0,
        name = "",
        status = DisputeForecastStatus.PLANNED,
    )
}
