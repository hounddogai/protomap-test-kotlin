package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize

// The fuel burn manifest records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a fuel burn manifest is in its life cycle. */
enum class FuelBurnManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel burn manifest. Its code is unique within a tenant. */
data class FuelBurnManifest(
    var code: String,
    var label: String,
    var archived: Boolean,
    var attempts: Int,
    var vehicleId: String,
    var status: FuelBurnManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnManifestStatus): Boolean {
        when (status) {
            FuelBurnManifestStatus.OPEN -> {
                when (next) {
                    FuelBurnManifestStatus.RESOLVED -> return true
                }
                return false
            }
            FuelBurnManifestStatus.IN_REVIEW -> return next == FuelBurnManifestStatus.REOPENED || next == FuelBurnManifestStatus.ARCHIVED
            FuelBurnManifestStatus.RESOLVED -> {
                when (next) {
                    FuelBurnManifestStatus.REOPENED, FuelBurnManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelBurnManifestStatus.REOPENED -> return next == FuelBurnManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelBurnManifestStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): FuelBurnManifest {
        return FuelBurnManifest(
            code = code,
            label = label,
            archived = archived,
            attempts = attempts,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel burn manifest in status open. */
fun newFuelBurnManifest(code: String, label: String): FuelBurnManifest {
    return FuelBurnManifest(
        code = code,
        label = label,
        archived = false,
        attempts = 0,
        vehicleId = "",
        status = FuelBurnManifestStatus.OPEN,
    )
}
