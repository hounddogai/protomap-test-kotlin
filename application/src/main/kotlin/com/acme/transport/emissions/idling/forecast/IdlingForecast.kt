package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling forecast records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a idling forecast is in its life cycle. */
enum class IdlingForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A idling forecast as the idling team keeps it. */
data class IdlingForecast(
    var code: String,
    var name: String,
    var routeCode: String,
    var amount: Double,
    var active: Boolean,
    var score: Double,
    var createdAt: Int,
    var depot: String,
    var country: String,
    var capacity: Int,
    var visible: Boolean,
    var status: IdlingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingForecastStatus): Boolean {
        when (status) {
            IdlingForecastStatus.OPEN -> {
                when (next) {
                    IdlingForecastStatus.RESOLVED -> return true
                }
                return false
            }
            IdlingForecastStatus.IN_REVIEW -> return next == IdlingForecastStatus.RESOLVED || next == IdlingForecastStatus.ARCHIVED
            IdlingForecastStatus.RESOLVED -> {
                when (next) {
                    IdlingForecastStatus.REOPENED -> return true
                }
                return false
            }
            IdlingForecastStatus.REOPENED -> {
                when (next) {
                    IdlingForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingForecastStatus) {
        if (!canMoveTo(next)) {
            error("idling forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"idling forecast"} $code"

    fun weightedExposure(): Double = round((createdAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): IdlingForecast {
        return IdlingForecast(
            code = code,
            name = name,
            routeCode = routeCode,
            amount = amount,
            active = active,
            score = score,
            createdAt = createdAt,
            depot = depot,
            country = country,
            capacity = capacity,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a idling forecast in status open. */
fun newIdlingForecast(code: String, name: String): IdlingForecast {
    return IdlingForecast(
        code = code,
        name = name,
        routeCode = "",
        amount = 0.0,
        active = false,
        score = 0.0,
        createdAt = 0,
        depot = "",
        country = "",
        capacity = 0,
        visible = false,
        status = IdlingForecastStatus.OPEN,
    )
}
