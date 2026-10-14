package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn profile records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn profile is in its life cycle. */
enum class FuelBurnProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel burn profile. Its code is unique within a tenant. */
data class FuelBurnProfile(
    var code: String,
    var label: String,
    var channel: String,
    var cost: Double,
    var startsAt: Int,
    var status: FuelBurnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnProfileStatus): Boolean {
        when (status) {
            FuelBurnProfileStatus.CREATED -> {
                when (next) {
                    FuelBurnProfileStatus.VALIDATED, FuelBurnProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            FuelBurnProfileStatus.VALIDATED -> {
                when (next) {
                    FuelBurnProfileStatus.PUBLISHED, FuelBurnProfileStatus.RETIRED -> return true
                }
                return false
            }
            FuelBurnProfileStatus.PUBLISHED -> {
                when (next) {
                    FuelBurnProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnProfileStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fuel burn profile"} $code"

    fun weightedExposure(): Double = round(cost * startsAt.toDouble() * 100) / 100

    fun snapshot(): FuelBurnProfile {
        return FuelBurnProfile(
            code = code,
            label = label,
            channel = channel,
            cost = cost,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fuel burn profile in status created. */
fun newFuelBurnProfile(code: String, label: String): FuelBurnProfile {
    return FuelBurnProfile(
        code = code,
        label = label,
        channel = "",
        cost = 0.0,
        startsAt = 0,
        status = FuelBurnProfileStatus.CREATED,
    )
}
