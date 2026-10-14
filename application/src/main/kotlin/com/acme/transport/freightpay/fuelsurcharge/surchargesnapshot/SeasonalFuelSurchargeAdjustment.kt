package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal fuel surcharge adjustment is in its life cycle. */
enum class SeasonalFuelSurchargeAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal fuel surcharge adjustment. Its code is unique within a tenant. */
data class SeasonalFuelSurchargeAdjustment(
    var code: String,
    var title: String,
    var attempts: Int,
    var carrier: String,
    var amount: Double,
    var contactEmail: String,
    var ratio: Double,
    var windowDays: Int,
    var startsAt: Int,
    var weight: Double,
    var pallets: Int,
    var status: SeasonalFuelSurchargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargeAdjustmentStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargeAdjustmentStatus.PROPOSED -> {
                when (next) {
                    SeasonalFuelSurchargeAdjustmentStatus.REJECTED, SeasonalFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalFuelSurchargeAdjustmentStatus.APPROVED -> return next == SeasonalFuelSurchargeAdjustmentStatus.WITHDRAWN
            SeasonalFuelSurchargeAdjustmentStatus.REJECTED -> return next == SeasonalFuelSurchargeAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round(startsAt.toDouble() * weight * 100) / 100

    fun snapshot(): SeasonalFuelSurchargeAdjustment {
        return SeasonalFuelSurchargeAdjustment(
            code = code,
            title = title,
            attempts = attempts,
            carrier = carrier,
            amount = amount,
            contactEmail = contactEmail,
            ratio = ratio,
            windowDays = windowDays,
            startsAt = startsAt,
            weight = weight,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal fuel surcharge adjustment in status proposed. */
fun newSeasonalFuelSurchargeAdjustment(code: String, title: String): SeasonalFuelSurchargeAdjustment {
    return SeasonalFuelSurchargeAdjustment(
        code = code,
        title = title,
        attempts = 0,
        carrier = "",
        amount = 0.0,
        contactEmail = "",
        ratio = 0.0,
        windowDays = 0,
        startsAt = 0,
        weight = 0.0,
        pallets = 0,
        status = SeasonalFuelSurchargeAdjustmentStatus.PROPOSED,
    )
}
