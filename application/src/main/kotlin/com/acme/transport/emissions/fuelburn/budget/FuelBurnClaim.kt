package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn claim is in its life cycle. */
enum class FuelBurnClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fuel burn claim and its current status. */
data class FuelBurnClaim(
    var code: String,
    var title: String,
    var category: String,
    var version: Int,
    var ownerId: String,
    var driverId: String,
    var minimum: Double,
    var trailerId: String,
    var status: FuelBurnClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnClaimStatus): Boolean {
        when (status) {
            FuelBurnClaimStatus.CREATED -> return next == FuelBurnClaimStatus.RETIRED
            FuelBurnClaimStatus.VALIDATED -> {
                when (next) {
                    FuelBurnClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            FuelBurnClaimStatus.PUBLISHED -> {
                when (next) {
                    FuelBurnClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnClaimStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((minimum - version.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnClaim {
        return FuelBurnClaim(
            code = code,
            title = title,
            category = category,
            version = version,
            ownerId = ownerId,
            driverId = driverId,
            minimum = minimum,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fuel burn claim in status created. */
fun newFuelBurnClaim(code: String, title: String): FuelBurnClaim {
    return FuelBurnClaim(
        code = code,
        title = title,
        category = "",
        version = 0,
        ownerId = "",
        driverId = "",
        minimum = 0.0,
        trailerId = "",
        status = FuelBurnClaimStatus.CREATED,
    )
}
