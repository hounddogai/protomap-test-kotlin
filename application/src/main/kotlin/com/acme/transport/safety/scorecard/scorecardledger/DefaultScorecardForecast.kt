package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default scorecard forecast is in its life cycle. */
enum class DefaultScorecardForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default scorecard forecast. Its code is unique within a tenant. */
data class DefaultScorecardForecast(
    var code: String,
    var name: String,
    var checksum: String,
    var dock: String,
    var category: String,
    var attempts: Int,
    var pallets: Int,
    var amount: Double,
    var region: String,
    var status: DefaultScorecardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardForecastStatus): Boolean {
        when (status) {
            DefaultScorecardForecastStatus.IDLE -> {
                when (next) {
                    DefaultScorecardForecastStatus.DRAINING -> return true
                }
                return false
            }
            DefaultScorecardForecastStatus.WARMING -> return next == DefaultScorecardForecastStatus.READY
            DefaultScorecardForecastStatus.READY -> return next == DefaultScorecardForecastStatus.DRAINING || next == DefaultScorecardForecastStatus.STOPPED
            DefaultScorecardForecastStatus.DRAINING -> return next == DefaultScorecardForecastStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultScorecardForecastStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((amount - pallets.toDouble()) * 100) / 100

    fun snapshot(): DefaultScorecardForecast {
        return DefaultScorecardForecast(
            code = code,
            name = name,
            checksum = checksum,
            dock = dock,
            category = category,
            attempts = attempts,
            pallets = pallets,
            amount = amount,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default scorecard forecast in status idle. */
fun newDefaultScorecardForecast(code: String, name: String): DefaultScorecardForecast {
    return DefaultScorecardForecast(
        code = code,
        name = name,
        checksum = "",
        dock = "",
        category = "",
        attempts = 0,
        pallets = 0,
        amount = 0.0,
        region = "",
        status = DefaultScorecardForecastStatus.IDLE,
    )
}
