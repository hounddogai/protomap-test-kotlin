package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel burn group is in its life cycle. */
enum class WeightedFuelBurnGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted fuel burn group and its current status. */
data class WeightedFuelBurnGroup(
    var code: String,
    var label: String,
    var depot: String,
    var distanceKm: Double,
    var pallets: Int,
    var notes: String,
    var vehicleId: String,
    var retries: Int,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var status: WeightedFuelBurnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnGroupStatus): Boolean {
        when (status) {
            WeightedFuelBurnGroupStatus.REQUESTED -> return next == WeightedFuelBurnGroupStatus.GRANTED || next == WeightedFuelBurnGroupStatus.REVOKED
            WeightedFuelBurnGroupStatus.GRANTED -> {
                when (next) {
                    WeightedFuelBurnGroupStatus.REVOKED, WeightedFuelBurnGroupStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedFuelBurnGroupStatus.REVOKED -> {
                when (next) {
                    WeightedFuelBurnGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnGroupStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((pallets.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): WeightedFuelBurnGroup {
        return WeightedFuelBurnGroup(
            code = code,
            label = label,
            depot = depot,
            distanceKm = distanceKm,
            pallets = pallets,
            notes = notes,
            vehicleId = vehicleId,
            retries = retries,
            shiftCode = shiftCode,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted fuel burn group in status requested. */
fun newWeightedFuelBurnGroup(code: String, label: String): WeightedFuelBurnGroup {
    return WeightedFuelBurnGroup(
        code = code,
        label = label,
        depot = "",
        distanceKm = 0.0,
        pallets = 0,
        notes = "",
        vehicleId = "",
        retries = 0,
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        status = WeightedFuelBurnGroupStatus.REQUESTED,
    )
}
