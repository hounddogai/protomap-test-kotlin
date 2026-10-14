package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.normalize

/** Where a global fuel surcharge override is in its life cycle. */
enum class GlobalFuelSurchargeOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global fuel surcharge override as the fuel surcharge team keeps it. */
data class GlobalFuelSurchargeOverride(
    var code: String,
    var label: String,
    var discount: Double,
    var routeCode: String,
    var ownerId: String,
    var name: String,
    var channel: String,
    var vehicleId: String,
    var status: GlobalFuelSurchargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeOverrideStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeOverrideStatus.REQUESTED -> {
                when (next) {
                    GlobalFuelSurchargeOverrideStatus.REVOKED -> return true
                }
                return false
            }
            GlobalFuelSurchargeOverrideStatus.GRANTED -> {
                when (next) {
                    GlobalFuelSurchargeOverrideStatus.REVOKED -> return true
                }
                return false
            }
            GlobalFuelSurchargeOverrideStatus.REVOKED -> return next == GlobalFuelSurchargeOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global fuel surcharge override"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): GlobalFuelSurchargeOverride {
        return GlobalFuelSurchargeOverride(
            code = code,
            label = label,
            discount = discount,
            routeCode = routeCode,
            ownerId = ownerId,
            name = name,
            channel = channel,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global fuel surcharge override in status requested. */
fun newGlobalFuelSurchargeOverride(code: String, label: String): GlobalFuelSurchargeOverride {
    return GlobalFuelSurchargeOverride(
        code = code,
        label = label,
        discount = 0.0,
        routeCode = "",
        ownerId = "",
        name = "",
        channel = "",
        vehicleId = "",
        status = GlobalFuelSurchargeOverrideStatus.REQUESTED,
    )
}
