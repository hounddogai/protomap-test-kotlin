package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize

/** Where a global drop forecast is in its life cycle. */
enum class GlobalDropForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global drop forecast, with what the planners need to act on it. */
data class GlobalDropForecast(
    var code: String,
    var title: String,
    var budget: Double,
    var region: String,
    var unit: String,
    var status: GlobalDropForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDropForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDropForecastStatus): Boolean {
        when (status) {
            GlobalDropForecastStatus.OPEN -> return next == GlobalDropForecastStatus.REOPENED
            GlobalDropForecastStatus.IN_REVIEW -> return next == GlobalDropForecastStatus.REOPENED
            GlobalDropForecastStatus.RESOLVED -> {
                when (next) {
                    GlobalDropForecastStatus.REOPENED, GlobalDropForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDropForecastStatus.REOPENED -> {
                when (next) {
                    GlobalDropForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDropForecastStatus) {
        if (!canMoveTo(next)) {
            error("global drop forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = budget

    fun snapshot(): GlobalDropForecast {
        return GlobalDropForecast(
            code = code,
            title = title,
            budget = budget,
            region = region,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global drop forecast in status open. */
fun newGlobalDropForecast(code: String, title: String): GlobalDropForecast {
    return GlobalDropForecast(
        code = code,
        title = title,
        budget = 0.0,
        region = "",
        unit = "",
        status = GlobalDropForecastStatus.OPEN,
    )
}
