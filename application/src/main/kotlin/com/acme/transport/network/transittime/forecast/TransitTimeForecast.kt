package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.normalize

// The transit time forecast records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time forecast is in its life cycle. */
enum class TransitTimeForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A transit time forecast. Its code is unique within a tenant. */
data class TransitTimeForecast(
    var code: String,
    var label: String,
    var externalRef: String,
    var routeCode: String,
    var trailerId: String,
    var etaMinutes: Int,
    var status: TransitTimeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeForecastStatus): Boolean {
        when (status) {
            TransitTimeForecastStatus.NEW -> {
                when (next) {
                    TransitTimeForecastStatus.QUEUED, TransitTimeForecastStatus.DONE -> return true
                }
                return false
            }
            TransitTimeForecastStatus.QUEUED -> {
                when (next) {
                    TransitTimeForecastStatus.DONE, TransitTimeForecastStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeForecastStatus.RUNNING -> return next == TransitTimeForecastStatus.DONE || next == TransitTimeForecastStatus.FAILED
            TransitTimeForecastStatus.DONE -> return next == TransitTimeForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TransitTimeForecastStatus) {
        if (!canMoveTo(next)) {
            error("transit time forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): TransitTimeForecast {
        return TransitTimeForecast(
            code = code,
            label = label,
            externalRef = externalRef,
            routeCode = routeCode,
            trailerId = trailerId,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a transit time forecast in status new. */
fun newTransitTimeForecast(code: String, label: String): TransitTimeForecast {
    return TransitTimeForecast(
        code = code,
        label = label,
        externalRef = "",
        routeCode = "",
        trailerId = "",
        etaMinutes = 0,
        status = TransitTimeForecastStatus.NEW,
    )
}
