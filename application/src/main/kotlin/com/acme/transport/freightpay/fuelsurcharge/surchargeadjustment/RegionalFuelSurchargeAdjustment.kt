package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel surcharge adjustment records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a regional fuel surcharge adjustment is in its life cycle. */
enum class RegionalFuelSurchargeAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional fuel surcharge adjustment, with what the planners need to act on it. */
data class RegionalFuelSurchargeAdjustment(
    var code: String,
    var label: String,
    var depot: String,
    var maximum: Double,
    var name: String,
    var distanceKm: Double,
    var graceDays: Int,
    var status: RegionalFuelSurchargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeAdjustmentStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeAdjustmentStatus.PLANNED -> {
                when (next) {
                    RegionalFuelSurchargeAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelSurchargeAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    RegionalFuelSurchargeAdjustmentStatus.COMPLETED, RegionalFuelSurchargeAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelSurchargeAdjustmentStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalFuelSurchargeAdjustmentStatus.COMPLETED, RegionalFuelSurchargeAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelSurchargeAdjustmentStatus.COMPLETED -> {
                when (next) {
                    RegionalFuelSurchargeAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((maximum + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelSurchargeAdjustment {
        return RegionalFuelSurchargeAdjustment(
            code = code,
            label = label,
            depot = depot,
            maximum = maximum,
            name = name,
            distanceKm = distanceKm,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional fuel surcharge adjustment in status planned. */
fun newRegionalFuelSurchargeAdjustment(code: String, label: String): RegionalFuelSurchargeAdjustment {
    return RegionalFuelSurchargeAdjustment(
        code = code,
        label = label,
        depot = "",
        maximum = 0.0,
        name = "",
        distanceKm = 0.0,
        graceDays = 0,
        status = RegionalFuelSurchargeAdjustmentStatus.PLANNED,
    )
}
