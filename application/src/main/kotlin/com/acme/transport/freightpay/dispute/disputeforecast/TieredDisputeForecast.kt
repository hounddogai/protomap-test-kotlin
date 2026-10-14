package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.normalize

// The tiered dispute forecast records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a tiered dispute forecast is in its life cycle. */
enum class TieredDisputeForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered dispute forecast, with what the planners need to act on it. */
data class TieredDisputeForecast(
    var code: String,
    var name: String,
    var channel: String,
    var country: String,
    var windowDays: Int,
    var status: TieredDisputeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDisputeForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDisputeForecastStatus): Boolean {
        when (status) {
            TieredDisputeForecastStatus.IDLE -> {
                when (next) {
                    TieredDisputeForecastStatus.STOPPED -> return true
                }
                return false
            }
            TieredDisputeForecastStatus.WARMING -> {
                when (next) {
                    TieredDisputeForecastStatus.DRAINING, TieredDisputeForecastStatus.STOPPED -> return true
                }
                return false
            }
            TieredDisputeForecastStatus.READY -> {
                when (next) {
                    TieredDisputeForecastStatus.DRAINING, TieredDisputeForecastStatus.STOPPED -> return true
                }
                return false
            }
            TieredDisputeForecastStatus.DRAINING -> {
                when (next) {
                    TieredDisputeForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDisputeForecastStatus) {
        if (!canMoveTo(next)) {
            error("tiered dispute forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): TieredDisputeForecast {
        return TieredDisputeForecast(
            code = code,
            name = name,
            channel = channel,
            country = country,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered dispute forecast in status idle. */
fun newTieredDisputeForecast(code: String, name: String): TieredDisputeForecast {
    return TieredDisputeForecast(
        code = code,
        name = name,
        channel = "",
        country = "",
        windowDays = 0,
        status = TieredDisputeForecastStatus.IDLE,
    )
}
