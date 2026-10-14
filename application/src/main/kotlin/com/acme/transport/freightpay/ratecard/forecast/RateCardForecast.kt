package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card forecast records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a rate card forecast is in its life cycle. */
enum class RateCardForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rate card forecast, with what the planners need to act on it. */
data class RateCardForecast(
    var code: String,
    var name: String,
    var lane: String,
    var active: Boolean,
    var amount: Double,
    var category: String,
    var batchSize: Int,
    var quantity: Int,
    var visible: Boolean,
    var status: RateCardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardForecastStatus): Boolean {
        when (status) {
            RateCardForecastStatus.OPEN -> {
                when (next) {
                    RateCardForecastStatus.RESOLVED -> return true
                }
                return false
            }
            RateCardForecastStatus.IN_REVIEW -> return next == RateCardForecastStatus.REOPENED || next == RateCardForecastStatus.ARCHIVED
            RateCardForecastStatus.RESOLVED -> return next == RateCardForecastStatus.REOPENED || next == RateCardForecastStatus.ARCHIVED
            RateCardForecastStatus.REOPENED -> {
                when (next) {
                    RateCardForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardForecastStatus) {
        if (!canMoveTo(next)) {
            error("rate card forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card forecast"} $code"

    fun netValue(): Double = round((batchSize.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RateCardForecast {
        return RateCardForecast(
            code = code,
            name = name,
            lane = lane,
            active = active,
            amount = amount,
            category = category,
            batchSize = batchSize,
            quantity = quantity,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rate card forecast in status open. */
fun newRateCardForecast(code: String, name: String): RateCardForecast {
    return RateCardForecast(
        code = code,
        name = name,
        lane = "",
        active = false,
        amount = 0.0,
        category = "",
        batchSize = 0,
        quantity = 0,
        visible = false,
        status = RateCardForecastStatus.OPEN,
    )
}
