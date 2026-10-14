package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback fuel surcharge forecast is in its life cycle. */
enum class FallbackFuelSurchargeForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback fuel surcharge forecast as the fuel surcharge team keeps it. */
data class FallbackFuelSurchargeForecast(
    var code: String,
    var name: String,
    var externalRef: String,
    var ceiling: Double,
    var durationMinutes: Int,
    var score: Double,
    var endsAt: Int,
    var cost: Double,
    var sequence: Int,
    var status: FallbackFuelSurchargeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelSurchargeForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelSurchargeForecastStatus): Boolean {
        when (status) {
            FallbackFuelSurchargeForecastStatus.IDLE -> {
                when (next) {
                    FallbackFuelSurchargeForecastStatus.STOPPED -> return true
                }
                return false
            }
            FallbackFuelSurchargeForecastStatus.WARMING -> return next == FallbackFuelSurchargeForecastStatus.READY
            FallbackFuelSurchargeForecastStatus.READY -> return next == FallbackFuelSurchargeForecastStatus.DRAINING
            FallbackFuelSurchargeForecastStatus.DRAINING -> {
                when (next) {
                    FallbackFuelSurchargeForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFuelSurchargeForecastStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel surcharge forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((endsAt.toDouble() - score) * 100) / 100

    fun snapshot(): FallbackFuelSurchargeForecast {
        return FallbackFuelSurchargeForecast(
            code = code,
            name = name,
            externalRef = externalRef,
            ceiling = ceiling,
            durationMinutes = durationMinutes,
            score = score,
            endsAt = endsAt,
            cost = cost,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback fuel surcharge forecast in status idle. */
fun newFallbackFuelSurchargeForecast(code: String, name: String): FallbackFuelSurchargeForecast {
    return FallbackFuelSurchargeForecast(
        code = code,
        name = name,
        externalRef = "",
        ceiling = 0.0,
        durationMinutes = 0,
        score = 0.0,
        endsAt = 0,
        cost = 0.0,
        sequence = 0,
        status = FallbackFuelSurchargeForecastStatus.IDLE,
    )
}
