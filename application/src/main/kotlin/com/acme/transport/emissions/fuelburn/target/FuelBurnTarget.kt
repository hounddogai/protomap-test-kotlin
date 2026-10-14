package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize

// The fuel burn target records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn target is in its life cycle. */
enum class FuelBurnTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel burn target as the fuel burn team keeps it. */
data class FuelBurnTarget(
    var code: String,
    var name: String,
    var channel: String,
    var checksum: String,
    var weight: Double,
    var status: FuelBurnTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnTargetStatus): Boolean {
        when (status) {
            FuelBurnTargetStatus.CREATED -> {
                when (next) {
                    FuelBurnTargetStatus.VALIDATED -> return true
                }
                return false
            }
            FuelBurnTargetStatus.VALIDATED -> return next == FuelBurnTargetStatus.PUBLISHED
            FuelBurnTargetStatus.PUBLISHED -> return next == FuelBurnTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnTargetStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = weight

    fun snapshot(): FuelBurnTarget {
        return FuelBurnTarget(
            code = code,
            name = name,
            channel = channel,
            checksum = checksum,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fuel burn target in status created. */
fun newFuelBurnTarget(code: String, name: String): FuelBurnTarget {
    return FuelBurnTarget(
        code = code,
        name = name,
        channel = "",
        checksum = "",
        weight = 0.0,
        status = FuelBurnTargetStatus.CREATED,
    )
}
