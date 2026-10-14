package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn cycle is in its life cycle. */
enum class FuelBurnCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fuel burn cycle and its current status. */
data class FuelBurnCycle(
    var code: String,
    var name: String,
    var phone: String,
    var rate: Double,
    var capacity: Int,
    var currency: String,
    var description: String,
    var weight: Double,
    var unit: String,
    var revision: Int,
    var status: FuelBurnCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnCycleStatus): Boolean {
        when (status) {
            FuelBurnCycleStatus.OPEN -> {
                when (next) {
                    FuelBurnCycleStatus.IN_REVIEW, FuelBurnCycleStatus.RESOLVED -> return true
                }
                return false
            }
            FuelBurnCycleStatus.IN_REVIEW -> {
                when (next) {
                    FuelBurnCycleStatus.RESOLVED, FuelBurnCycleStatus.REOPENED -> return true
                }
                return false
            }
            FuelBurnCycleStatus.RESOLVED -> return next == FuelBurnCycleStatus.REOPENED
            FuelBurnCycleStatus.REOPENED -> {
                when (next) {
                    FuelBurnCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnCycleStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn cycle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((weight + rate) * 100) / 100

    fun snapshot(): FuelBurnCycle {
        return FuelBurnCycle(
            code = code,
            name = name,
            phone = phone,
            rate = rate,
            capacity = capacity,
            currency = currency,
            description = description,
            weight = weight,
            unit = unit,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel burn cycle in status open. */
fun newFuelBurnCycle(code: String, name: String): FuelBurnCycle {
    return FuelBurnCycle(
        code = code,
        name = name,
        phone = "",
        rate = 0.0,
        capacity = 0,
        currency = "",
        description = "",
        weight = 0.0,
        unit = "",
        revision = 0,
        status = FuelBurnCycleStatus.OPEN,
    )
}
