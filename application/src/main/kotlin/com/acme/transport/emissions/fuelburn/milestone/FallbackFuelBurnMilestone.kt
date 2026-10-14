package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback fuel burn milestone records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a fallback fuel burn milestone is in its life cycle. */
enum class FallbackFuelBurnMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback fuel burn milestone and its current status. */
data class FallbackFuelBurnMilestone(
    var code: String,
    var title: String,
    var stopCount: Int,
    var notes: String,
    var durationMinutes: Int,
    var discount: Double,
    var vehicleId: String,
    var description: String,
    var loadKg: Double,
    var status: FallbackFuelBurnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelBurnMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelBurnMilestoneStatus): Boolean {
        when (status) {
            FallbackFuelBurnMilestoneStatus.OPEN -> {
                when (next) {
                    FallbackFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackFuelBurnMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    FallbackFuelBurnMilestoneStatus.REOPENED, FallbackFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackFuelBurnMilestoneStatus.RESOLVED -> return next == FallbackFuelBurnMilestoneStatus.REOPENED || next == FallbackFuelBurnMilestoneStatus.ARCHIVED
            FallbackFuelBurnMilestoneStatus.REOPENED -> {
                when (next) {
                    FallbackFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFuelBurnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel burn milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round((durationMinutes.toDouble() + discount) * 100) / 100

    fun snapshot(): FallbackFuelBurnMilestone {
        return FallbackFuelBurnMilestone(
            code = code,
            title = title,
            stopCount = stopCount,
            notes = notes,
            durationMinutes = durationMinutes,
            discount = discount,
            vehicleId = vehicleId,
            description = description,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback fuel burn milestone in status open. */
fun newFallbackFuelBurnMilestone(code: String, title: String): FallbackFuelBurnMilestone {
    return FallbackFuelBurnMilestone(
        code = code,
        title = title,
        stopCount = 0,
        notes = "",
        durationMinutes = 0,
        discount = 0.0,
        vehicleId = "",
        description = "",
        loadKg = 0.0,
        status = FallbackFuelBurnMilestoneStatus.OPEN,
    )
}
