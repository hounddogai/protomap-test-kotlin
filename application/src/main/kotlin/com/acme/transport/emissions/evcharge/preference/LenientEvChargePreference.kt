package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient ev charge preference is in its life cycle. */
enum class LenientEvChargePreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient ev charge preference as the ev charge team keeps it. */
data class LenientEvChargePreference(
    var code: String,
    var label: String,
    var ceiling: Double,
    var fuelLiters: Double,
    var vehicleId: String,
    var status: LenientEvChargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEvChargePreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEvChargePreferenceStatus): Boolean {
        when (status) {
            LenientEvChargePreferenceStatus.CREATED -> return next == LenientEvChargePreferenceStatus.RETIRED
            LenientEvChargePreferenceStatus.VALIDATED -> return next == LenientEvChargePreferenceStatus.PUBLISHED || next == LenientEvChargePreferenceStatus.RETIRED
            LenientEvChargePreferenceStatus.PUBLISHED -> return next == LenientEvChargePreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientEvChargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lenient ev charge preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round((ceiling - fuelLiters) * 100) / 100

    fun snapshot(): LenientEvChargePreference {
        return LenientEvChargePreference(
            code = code,
            label = label,
            ceiling = ceiling,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient ev charge preference in status created. */
fun newLenientEvChargePreference(code: String, label: String): LenientEvChargePreference {
    return LenientEvChargePreference(
        code = code,
        label = label,
        ceiling = 0.0,
        fuelLiters = 0.0,
        vehicleId = "",
        status = LenientEvChargePreferenceStatus.CREATED,
    )
}
