package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.normalize

/** Where a fuel burn mapping is in its life cycle. */
enum class FuelBurnMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fuel burn mapping as the fuel burn team keeps it. */
data class FuelBurnMapping(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var name: String,
    var label: String,
    var minimum: Double,
    var vehicleId: String,
    var status: FuelBurnMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnMappingStatus): Boolean {
        when (status) {
            FuelBurnMappingStatus.NEW -> return next == FuelBurnMappingStatus.QUEUED
            FuelBurnMappingStatus.QUEUED -> {
                when (next) {
                    FuelBurnMappingStatus.RUNNING, FuelBurnMappingStatus.DONE -> return true
                }
                return false
            }
            FuelBurnMappingStatus.RUNNING -> {
                when (next) {
                    FuelBurnMappingStatus.DONE -> return true
                }
                return false
            }
            FuelBurnMappingStatus.DONE -> return next == FuelBurnMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelBurnMappingStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel burn mapping"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): FuelBurnMapping {
        return FuelBurnMapping(
            code = code,
            title = title,
            pinned = pinned,
            name = name,
            label = label,
            minimum = minimum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn mapping in status new. */
fun newFuelBurnMapping(code: String, title: String): FuelBurnMapping {
    return FuelBurnMapping(
        code = code,
        title = title,
        pinned = false,
        name = "",
        label = "",
        minimum = 0.0,
        vehicleId = "",
        status = FuelBurnMappingStatus.NEW,
    )
}
