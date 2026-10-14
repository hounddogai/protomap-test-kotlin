package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard forecast is in its life cycle. */
enum class ScorecardForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scorecard forecast, with what the planners need to act on it. */
data class ScorecardForecast(
    var code: String,
    var name: String,
    var startsAt: Int,
    var stopCount: Int,
    var summary: String,
    var checksum: String,
    var discount: Double,
    var phone: String,
    var quantity: Int,
    var status: ScorecardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardForecastStatus): Boolean {
        when (status) {
            ScorecardForecastStatus.REQUESTED -> {
                when (next) {
                    ScorecardForecastStatus.GRANTED, ScorecardForecastStatus.REVOKED -> return true
                }
                return false
            }
            ScorecardForecastStatus.GRANTED -> return next == ScorecardForecastStatus.REVOKED || next == ScorecardForecastStatus.EXPIRED
            ScorecardForecastStatus.REVOKED -> {
                when (next) {
                    ScorecardForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardForecastStatus) {
        if (!canMoveTo(next)) {
            error("scorecard forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((quantity.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ScorecardForecast {
        return ScorecardForecast(
            code = code,
            name = name,
            startsAt = startsAt,
            stopCount = stopCount,
            summary = summary,
            checksum = checksum,
            discount = discount,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard forecast in status requested. */
fun newScorecardForecast(code: String, name: String): ScorecardForecast {
    return ScorecardForecast(
        code = code,
        name = name,
        startsAt = 0,
        stopCount = 0,
        summary = "",
        checksum = "",
        discount = 0.0,
        phone = "",
        quantity = 0,
        status = ScorecardForecastStatus.REQUESTED,
    )
}
