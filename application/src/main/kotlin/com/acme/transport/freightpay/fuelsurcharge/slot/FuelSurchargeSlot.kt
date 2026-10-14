package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge slot records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge slot is in its life cycle. */
enum class FuelSurchargeSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One fuel surcharge slot, with what the planners need to act on it. */
data class FuelSurchargeSlot(
    var code: String,
    var title: String,
    var driverId: String,
    var pallets: Int,
    var stopCount: Int,
    var status: FuelSurchargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeSlotStatus): Boolean {
        when (status) {
            FuelSurchargeSlotStatus.CREATED -> {
                when (next) {
                    FuelSurchargeSlotStatus.VALIDATED, FuelSurchargeSlotStatus.RETIRED -> return true
                }
                return false
            }
            FuelSurchargeSlotStatus.VALIDATED -> {
                when (next) {
                    FuelSurchargeSlotStatus.PUBLISHED -> return true
                }
                return false
            }
            FuelSurchargeSlotStatus.PUBLISHED -> return next == FuelSurchargeSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((pallets.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeSlot {
        return FuelSurchargeSlot(
            code = code,
            title = title,
            driverId = driverId,
            pallets = pallets,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge slot in status created. */
fun newFuelSurchargeSlot(code: String, title: String): FuelSurchargeSlot {
    return FuelSurchargeSlot(
        code = code,
        title = title,
        driverId = "",
        pallets = 0,
        stopCount = 0,
        status = FuelSurchargeSlotStatus.CREATED,
    )
}
