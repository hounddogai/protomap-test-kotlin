package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient fuel surcharge preference records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a lenient fuel surcharge preference is in its life cycle. */
enum class LenientFuelSurchargePreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient fuel surcharge preference as the fuel surcharge team keeps it. */
data class LenientFuelSurchargePreference(
    var code: String,
    var label: String,
    var depot: String,
    var version: Int,
    var driverId: String,
    var batchSize: Int,
    var currency: String,
    var status: LenientFuelSurchargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelSurchargePreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelSurchargePreferenceStatus): Boolean {
        when (status) {
            LenientFuelSurchargePreferenceStatus.REQUESTED -> return next == LenientFuelSurchargePreferenceStatus.GRANTED || next == LenientFuelSurchargePreferenceStatus.REVOKED
            LenientFuelSurchargePreferenceStatus.GRANTED -> return next == LenientFuelSurchargePreferenceStatus.EXPIRED
            LenientFuelSurchargePreferenceStatus.REVOKED -> {
                when (next) {
                    LenientFuelSurchargePreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFuelSurchargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel surcharge preference $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((batchSize.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): LenientFuelSurchargePreference {
        return LenientFuelSurchargePreference(
            code = code,
            label = label,
            depot = depot,
            version = version,
            driverId = driverId,
            batchSize = batchSize,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient fuel surcharge preference in status requested. */
fun newLenientFuelSurchargePreference(code: String, label: String): LenientFuelSurchargePreference {
    return LenientFuelSurchargePreference(
        code = code,
        label = label,
        depot = "",
        version = 0,
        driverId = "",
        batchSize = 0,
        currency = "",
        status = LenientFuelSurchargePreferenceStatus.REQUESTED,
    )
}
