package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge request records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge request is in its life cycle. */
enum class FuelSurchargeRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fuel surcharge request as the fuel surcharge team keeps it. */
data class FuelSurchargeRequest(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var endsAt: Int,
    var etaMinutes: Int,
    var status: FuelSurchargeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeRequestStatus): Boolean {
        when (status) {
            FuelSurchargeRequestStatus.REQUESTED -> return next == FuelSurchargeRequestStatus.EXPIRED
            FuelSurchargeRequestStatus.GRANTED -> return next == FuelSurchargeRequestStatus.REVOKED || next == FuelSurchargeRequestStatus.EXPIRED
            FuelSurchargeRequestStatus.REVOKED -> return next == FuelSurchargeRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeRequestStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round(endsAt.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): FuelSurchargeRequest {
        return FuelSurchargeRequest(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge request in status requested. */
fun newFuelSurchargeRequest(code: String, label: String): FuelSurchargeRequest {
    return FuelSurchargeRequest(
        code = code,
        label = label,
        fuelLiters = 0.0,
        endsAt = 0,
        etaMinutes = 0,
        status = FuelSurchargeRequestStatus.REQUESTED,
    )
}
