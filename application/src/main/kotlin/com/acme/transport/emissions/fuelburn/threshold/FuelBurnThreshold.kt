package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn threshold records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a fuel burn threshold is in its life cycle. */
enum class FuelBurnThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fuel burn threshold and its current status. */
data class FuelBurnThreshold(
    var code: String,
    var name: String,
    var priority: Int,
    var reviewer: String,
    var contactEmail: String,
    var budget: Double,
    var graceDays: Int,
    var loadKg: Double,
    var pinned: Boolean,
    var approved: Boolean,
    var status: FuelBurnThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnThresholdStatus): Boolean {
        when (status) {
            FuelBurnThresholdStatus.CREATED -> return next == FuelBurnThresholdStatus.VALIDATED
            FuelBurnThresholdStatus.VALIDATED -> {
                when (next) {
                    FuelBurnThresholdStatus.PUBLISHED, FuelBurnThresholdStatus.RETIRED -> return true
                }
                return false
            }
            FuelBurnThresholdStatus.PUBLISHED -> return next == FuelBurnThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn threshold"} $code"

    fun weightedValue(): Double = round((loadKg + budget) * 100) / 100

    fun snapshot(): FuelBurnThreshold {
        return FuelBurnThreshold(
            code = code,
            name = name,
            priority = priority,
            reviewer = reviewer,
            contactEmail = contactEmail,
            budget = budget,
            graceDays = graceDays,
            loadKg = loadKg,
            pinned = pinned,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn threshold in status created. */
fun newFuelBurnThreshold(code: String, name: String): FuelBurnThreshold {
    return FuelBurnThreshold(
        code = code,
        name = name,
        priority = 0,
        reviewer = "",
        contactEmail = "",
        budget = 0.0,
        graceDays = 0,
        loadKg = 0.0,
        pinned = false,
        approved = false,
        status = FuelBurnThresholdStatus.CREATED,
    )
}
