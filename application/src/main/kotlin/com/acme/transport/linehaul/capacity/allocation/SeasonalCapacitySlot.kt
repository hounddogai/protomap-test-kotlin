package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize

/** Where a seasonal capacity slot is in its life cycle. */
enum class SeasonalCapacitySlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal capacity slot as the capacity team keeps it. */
data class SeasonalCapacitySlot(
    var code: String,
    var label: String,
    var carrier: String,
    var distanceKm: Double,
    var region: String,
    var status: SeasonalCapacitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacitySlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacitySlotStatus): Boolean {
        when (status) {
            SeasonalCapacitySlotStatus.REQUESTED -> {
                when (next) {
                    SeasonalCapacitySlotStatus.GRANTED, SeasonalCapacitySlotStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalCapacitySlotStatus.GRANTED -> return next == SeasonalCapacitySlotStatus.REVOKED || next == SeasonalCapacitySlotStatus.EXPIRED
            SeasonalCapacitySlotStatus.REVOKED -> {
                when (next) {
                    SeasonalCapacitySlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCapacitySlotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): SeasonalCapacitySlot {
        return SeasonalCapacitySlot(
            code = code,
            label = label,
            carrier = carrier,
            distanceKm = distanceKm,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal capacity slot in status requested. */
fun newSeasonalCapacitySlot(code: String, label: String): SeasonalCapacitySlot {
    return SeasonalCapacitySlot(
        code = code,
        label = label,
        carrier = "",
        distanceKm = 0.0,
        region = "",
        status = SeasonalCapacitySlotStatus.REQUESTED,
    )
}
