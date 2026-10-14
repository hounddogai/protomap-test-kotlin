package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.normalize

/** Where a regional idling forecast is in its life cycle. */
enum class RegionalIdlingForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional idling forecast, with what the planners need to act on it. */
data class RegionalIdlingForecast(
    var code: String,
    var title: String,
    var shiftCode: String,
    var archived: Boolean,
    var ownerId: String,
    var score: Double,
    var status: RegionalIdlingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingForecastStatus): Boolean {
        when (status) {
            RegionalIdlingForecastStatus.DRAFT -> {
                when (next) {
                    RegionalIdlingForecastStatus.PENDING, RegionalIdlingForecastStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalIdlingForecastStatus.PENDING -> return next == RegionalIdlingForecastStatus.SUSPENDED
            RegionalIdlingForecastStatus.ACTIVE -> return next == RegionalIdlingForecastStatus.CLOSED
            RegionalIdlingForecastStatus.SUSPENDED -> return next == RegionalIdlingForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalIdlingForecastStatus) {
        if (!canMoveTo(next)) {
            error("regional idling forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = score

    fun snapshot(): RegionalIdlingForecast {
        return RegionalIdlingForecast(
            code = code,
            title = title,
            shiftCode = shiftCode,
            archived = archived,
            ownerId = ownerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional idling forecast in status draft. */
fun newRegionalIdlingForecast(code: String, title: String): RegionalIdlingForecast {
    return RegionalIdlingForecast(
        code = code,
        title = title,
        shiftCode = "",
        archived = false,
        ownerId = "",
        score = 0.0,
        status = RegionalIdlingForecastStatus.DRAFT,
    )
}
