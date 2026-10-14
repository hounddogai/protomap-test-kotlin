package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire forecast is in its life cycle. */
enum class TireForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tire forecast. Its code is unique within a tenant. */
data class TireForecast(
    var code: String,
    var label: String,
    var checksum: String,
    var region: String,
    var ratio: Double,
    var rate: Double,
    var description: String,
    var amount: Double,
    var status: TireForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireForecastStatus): Boolean {
        when (status) {
            TireForecastStatus.PROPOSED -> return next == TireForecastStatus.REJECTED
            TireForecastStatus.APPROVED -> {
                when (next) {
                    TireForecastStatus.REJECTED, TireForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            TireForecastStatus.REJECTED -> return next == TireForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TireForecastStatus) {
        if (!canMoveTo(next)) {
            error("tire forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire forecast"} $code"

    fun weightedExposure(): Double = round((rate + amount) * 100) / 100

    fun snapshot(): TireForecast {
        return TireForecast(
            code = code,
            label = label,
            checksum = checksum,
            region = region,
            ratio = ratio,
            rate = rate,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tire forecast in status proposed. */
fun newTireForecast(code: String, label: String): TireForecast {
    return TireForecast(
        code = code,
        label = label,
        checksum = "",
        region = "",
        ratio = 0.0,
        rate = 0.0,
        description = "",
        amount = 0.0,
        status = TireForecastStatus.PROPOSED,
    )
}
