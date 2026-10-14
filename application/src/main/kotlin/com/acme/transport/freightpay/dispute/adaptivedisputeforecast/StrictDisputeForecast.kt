package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict dispute forecast records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a strict dispute forecast is in its life cycle. */
enum class StrictDisputeForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict dispute forecast as the dispute team keeps it. */
data class StrictDisputeForecast(
    var code: String,
    var name: String,
    var title: String,
    var channel: String,
    var budget: Double,
    var category: String,
    var loadKg: Double,
    var status: StrictDisputeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDisputeForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDisputeForecastStatus): Boolean {
        when (status) {
            StrictDisputeForecastStatus.OPEN -> {
                when (next) {
                    StrictDisputeForecastStatus.REOPENED -> return true
                }
                return false
            }
            StrictDisputeForecastStatus.IN_REVIEW -> {
                when (next) {
                    StrictDisputeForecastStatus.RESOLVED -> return true
                }
                return false
            }
            StrictDisputeForecastStatus.RESOLVED -> {
                when (next) {
                    StrictDisputeForecastStatus.REOPENED -> return true
                }
                return false
            }
            StrictDisputeForecastStatus.REOPENED -> return next == StrictDisputeForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDisputeForecastStatus) {
        if (!canMoveTo(next)) {
            error("strict dispute forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round(loadKg * budget * 100) / 100

    fun snapshot(): StrictDisputeForecast {
        return StrictDisputeForecast(
            code = code,
            name = name,
            title = title,
            channel = channel,
            budget = budget,
            category = category,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict dispute forecast in status open. */
fun newStrictDisputeForecast(code: String, name: String): StrictDisputeForecast {
    return StrictDisputeForecast(
        code = code,
        name = name,
        title = "",
        channel = "",
        budget = 0.0,
        category = "",
        loadKg = 0.0,
        status = StrictDisputeForecastStatus.OPEN,
    )
}
