package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy fuel surcharge window is in its life cycle. */
enum class LegacyFuelSurchargeWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy fuel surcharge window and its current status. */
data class LegacyFuelSurchargeWindow(
    var code: String,
    var name: String,
    var lane: String,
    var retries: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var status: LegacyFuelSurchargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelSurchargeWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelSurchargeWindowStatus): Boolean {
        when (status) {
            LegacyFuelSurchargeWindowStatus.PLANNED -> {
                when (next) {
                    LegacyFuelSurchargeWindowStatus.SCHEDULED, LegacyFuelSurchargeWindowStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyFuelSurchargeWindowStatus.SCHEDULED -> return next == LegacyFuelSurchargeWindowStatus.CANCELLED
            LegacyFuelSurchargeWindowStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyFuelSurchargeWindowStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyFuelSurchargeWindowStatus.COMPLETED -> return next == LegacyFuelSurchargeWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyFuelSurchargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel surcharge window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy fuel surcharge window"} $code"

    fun effectiveExposure(): Double = round((fuelLiters - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyFuelSurchargeWindow {
        return LegacyFuelSurchargeWindow(
            code = code,
            name = name,
            lane = lane,
            retries = retries,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy fuel surcharge window in status planned. */
fun newLegacyFuelSurchargeWindow(code: String, name: String): LegacyFuelSurchargeWindow {
    return LegacyFuelSurchargeWindow(
        code = code,
        name = name,
        lane = "",
        retries = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        status = LegacyFuelSurchargeWindowStatus.PLANNED,
    )
}
