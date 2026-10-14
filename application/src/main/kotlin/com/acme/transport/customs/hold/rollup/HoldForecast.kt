package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold forecast is in its life cycle. */
enum class HoldForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hold forecast. Its code is unique within a tenant. */
data class HoldForecast(
    var code: String,
    var name: String,
    var score: Double,
    var externalRef: String,
    var currency: String,
    var margin: Double,
    var startsAt: Int,
    var status: HoldForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldForecastStatus): Boolean {
        when (status) {
            HoldForecastStatus.DRAFT -> {
                when (next) {
                    HoldForecastStatus.ACTIVE -> return true
                }
                return false
            }
            HoldForecastStatus.PENDING -> {
                when (next) {
                    HoldForecastStatus.ACTIVE, HoldForecastStatus.CLOSED -> return true
                }
                return false
            }
            HoldForecastStatus.ACTIVE -> {
                when (next) {
                    HoldForecastStatus.CLOSED -> return true
                }
                return false
            }
            HoldForecastStatus.SUSPENDED -> return next == HoldForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HoldForecastStatus) {
        if (!canMoveTo(next)) {
            error("hold forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((startsAt.toDouble() + score) * 100) / 100

    fun snapshot(): HoldForecast {
        return HoldForecast(
            code = code,
            name = name,
            score = score,
            externalRef = externalRef,
            currency = currency,
            margin = margin,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold forecast in status draft. */
fun newHoldForecast(code: String, name: String): HoldForecast {
    return HoldForecast(
        code = code,
        name = name,
        score = 0.0,
        externalRef = "",
        currency = "",
        margin = 0.0,
        startsAt = 0,
        status = HoldForecastStatus.DRAFT,
    )
}
