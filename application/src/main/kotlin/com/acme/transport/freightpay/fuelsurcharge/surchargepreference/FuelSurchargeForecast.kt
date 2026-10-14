package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge forecast is in its life cycle. */
enum class FuelSurchargeForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel surcharge forecast, with what the planners need to act on it. */
data class FuelSurchargeForecast(
    var code: String,
    var name: String,
    var phone: String,
    var attempts: Int,
    var depot: String,
    var ownerId: String,
    var reviewer: String,
    var etaMinutes: Int,
    var retries: Int,
    var status: FuelSurchargeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeForecastStatus): Boolean {
        when (status) {
            FuelSurchargeForecastStatus.IDLE -> return next == FuelSurchargeForecastStatus.READY || next == FuelSurchargeForecastStatus.STOPPED
            FuelSurchargeForecastStatus.WARMING -> {
                when (next) {
                    FuelSurchargeForecastStatus.READY -> return true
                }
                return false
            }
            FuelSurchargeForecastStatus.READY -> return next == FuelSurchargeForecastStatus.STOPPED
            FuelSurchargeForecastStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeForecastStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge forecast"} $code"

    fun netLoad(): Double = round((etaMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeForecast {
        return FuelSurchargeForecast(
            code = code,
            name = name,
            phone = phone,
            attempts = attempts,
            depot = depot,
            ownerId = ownerId,
            reviewer = reviewer,
            etaMinutes = etaMinutes,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel surcharge forecast in status idle. */
fun newFuelSurchargeForecast(code: String, name: String): FuelSurchargeForecast {
    return FuelSurchargeForecast(
        code = code,
        name = name,
        phone = "",
        attempts = 0,
        depot = "",
        ownerId = "",
        reviewer = "",
        etaMinutes = 0,
        retries = 0,
        status = FuelSurchargeForecastStatus.IDLE,
    )
}
