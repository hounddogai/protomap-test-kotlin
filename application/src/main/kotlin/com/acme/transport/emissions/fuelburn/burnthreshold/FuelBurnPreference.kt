package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn preference is in its life cycle. */
enum class FuelBurnPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fuel burn preference as the fuel burn team keeps it. */
data class FuelBurnPreference(
    var code: String,
    var label: String,
    var quantity: Int,
    var endsAt: Int,
    var etaMinutes: Int,
    var cost: Double,
    var minimum: Double,
    var pinned: Boolean,
    var driverId: String,
    var shiftCode: String,
    var capacity: Int,
    var status: FuelBurnPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnPreferenceStatus): Boolean {
        when (status) {
            FuelBurnPreferenceStatus.PLANNED -> {
                when (next) {
                    FuelBurnPreferenceStatus.SCHEDULED -> return true
                }
                return false
            }
            FuelBurnPreferenceStatus.SCHEDULED -> return next == FuelBurnPreferenceStatus.COMPLETED || next == FuelBurnPreferenceStatus.CANCELLED
            FuelBurnPreferenceStatus.IN_PROGRESS -> return next == FuelBurnPreferenceStatus.CANCELLED
            FuelBurnPreferenceStatus.COMPLETED -> {
                when (next) {
                    FuelBurnPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fuel burn preference"} $code"

    fun adjustedTotal(): Double = round((endsAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnPreference {
        return FuelBurnPreference(
            code = code,
            label = label,
            quantity = quantity,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            cost = cost,
            minimum = minimum,
            pinned = pinned,
            driverId = driverId,
            shiftCode = shiftCode,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel burn preference in status planned. */
fun newFuelBurnPreference(code: String, label: String): FuelBurnPreference {
    return FuelBurnPreference(
        code = code,
        label = label,
        quantity = 0,
        endsAt = 0,
        etaMinutes = 0,
        cost = 0.0,
        minimum = 0.0,
        pinned = false,
        driverId = "",
        shiftCode = "",
        capacity = 0,
        status = FuelBurnPreferenceStatus.PLANNED,
    )
}
