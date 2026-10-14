package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge claim is in its life cycle. */
enum class FuelSurchargeClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fuel surcharge claim, with what the planners need to act on it. */
data class FuelSurchargeClaim(
    var code: String,
    var title: String,
    var cost: Double,
    var graceDays: Int,
    var quantity: Int,
    var retries: Int,
    var odometer: Int,
    var driverId: String,
    var status: FuelSurchargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeClaimStatus): Boolean {
        when (status) {
            FuelSurchargeClaimStatus.NEW -> return next == FuelSurchargeClaimStatus.RUNNING
            FuelSurchargeClaimStatus.QUEUED -> return next == FuelSurchargeClaimStatus.FAILED
            FuelSurchargeClaimStatus.RUNNING -> return next == FuelSurchargeClaimStatus.DONE || next == FuelSurchargeClaimStatus.FAILED
            FuelSurchargeClaimStatus.DONE -> return next == FuelSurchargeClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((graceDays.toDouble() - cost) * 100) / 100

    fun snapshot(): FuelSurchargeClaim {
        return FuelSurchargeClaim(
            code = code,
            title = title,
            cost = cost,
            graceDays = graceDays,
            quantity = quantity,
            retries = retries,
            odometer = odometer,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge claim in status new. */
fun newFuelSurchargeClaim(code: String, title: String): FuelSurchargeClaim {
    return FuelSurchargeClaim(
        code = code,
        title = title,
        cost = 0.0,
        graceDays = 0,
        quantity = 0,
        retries = 0,
        odometer = 0,
        driverId = "",
        status = FuelSurchargeClaimStatus.NEW,
    )
}
