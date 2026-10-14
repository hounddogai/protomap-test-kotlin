package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn group records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn group is in its life cycle. */
enum class FuelBurnGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fuel burn group and its current status. */
data class FuelBurnGroup(
    var code: String,
    var name: String,
    var vehicleId: String,
    var revision: Int,
    var sequence: Int,
    var lane: String,
    var budget: Double,
    var odometer: Int,
    var segment: String,
    var approved: Boolean,
    var status: FuelBurnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnGroupStatus): Boolean {
        when (status) {
            FuelBurnGroupStatus.NEW -> return next == FuelBurnGroupStatus.QUEUED || next == FuelBurnGroupStatus.FAILED
            FuelBurnGroupStatus.QUEUED -> {
                when (next) {
                    FuelBurnGroupStatus.RUNNING -> return true
                }
                return false
            }
            FuelBurnGroupStatus.RUNNING -> return next == FuelBurnGroupStatus.FAILED
            FuelBurnGroupStatus.DONE -> {
                when (next) {
                    FuelBurnGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnGroupStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn group $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"fuel burn group"} $code"

    fun weightedTotal(): Double = round((odometer.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnGroup {
        return FuelBurnGroup(
            code = code,
            name = name,
            vehicleId = vehicleId,
            revision = revision,
            sequence = sequence,
            lane = lane,
            budget = budget,
            odometer = odometer,
            segment = segment,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fuel burn group in status new. */
fun newFuelBurnGroup(code: String, name: String): FuelBurnGroup {
    return FuelBurnGroup(
        code = code,
        name = name,
        vehicleId = "",
        revision = 0,
        sequence = 0,
        lane = "",
        budget = 0.0,
        odometer = 0,
        segment = "",
        approved = false,
        status = FuelBurnGroupStatus.NEW,
    )
}
