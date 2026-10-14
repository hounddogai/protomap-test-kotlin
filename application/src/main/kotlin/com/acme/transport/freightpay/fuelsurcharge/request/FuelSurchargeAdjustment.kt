package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge adjustment is in its life cycle. */
enum class FuelSurchargeAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fuel surcharge adjustment, with what the planners need to act on it. */
data class FuelSurchargeAdjustment(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var ratio: Double,
    var pinned: Boolean,
    var visible: Boolean,
    var retries: Int,
    var shiftCode: String,
    var status: FuelSurchargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeAdjustmentStatus): Boolean {
        when (status) {
            FuelSurchargeAdjustmentStatus.NEW -> {
                when (next) {
                    FuelSurchargeAdjustmentStatus.DONE -> return true
                }
                return false
            }
            FuelSurchargeAdjustmentStatus.QUEUED -> return next == FuelSurchargeAdjustmentStatus.RUNNING || next == FuelSurchargeAdjustmentStatus.FAILED
            FuelSurchargeAdjustmentStatus.RUNNING -> return next == FuelSurchargeAdjustmentStatus.FAILED
            FuelSurchargeAdjustmentStatus.DONE -> {
                when (next) {
                    FuelSurchargeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((distanceKm + ratio) * 100) / 100

    fun snapshot(): FuelSurchargeAdjustment {
        return FuelSurchargeAdjustment(
            code = code,
            name = name,
            distanceKm = distanceKm,
            ratio = ratio,
            pinned = pinned,
            visible = visible,
            retries = retries,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge adjustment in status new. */
fun newFuelSurchargeAdjustment(code: String, name: String): FuelSurchargeAdjustment {
    return FuelSurchargeAdjustment(
        code = code,
        name = name,
        distanceKm = 0.0,
        ratio = 0.0,
        pinned = false,
        visible = false,
        retries = 0,
        shiftCode = "",
        status = FuelSurchargeAdjustmentStatus.NEW,
    )
}
