package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted fuel burn estimate records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a weighted fuel burn estimate is in its life cycle. */
enum class WeightedFuelBurnEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted fuel burn estimate as the fuel burn team keeps it. */
data class WeightedFuelBurnEstimate(
    var code: String,
    var name: String,
    var segment: String,
    var depot: String,
    var maximum: Double,
    var quantity: Int,
    var cost: Double,
    var summary: String,
    var fuelLiters: Double,
    var floorValue: Double,
    var region: String,
    var status: WeightedFuelBurnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnEstimateStatus): Boolean {
        when (status) {
            WeightedFuelBurnEstimateStatus.REQUESTED -> {
                when (next) {
                    WeightedFuelBurnEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedFuelBurnEstimateStatus.GRANTED -> return next == WeightedFuelBurnEstimateStatus.REVOKED
            WeightedFuelBurnEstimateStatus.REVOKED -> {
                when (next) {
                    WeightedFuelBurnEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted fuel burn estimate"} $code"

    fun effectiveTotal(): Double = round((quantity.toDouble() - cost) * 100) / 100

    fun snapshot(): WeightedFuelBurnEstimate {
        return WeightedFuelBurnEstimate(
            code = code,
            name = name,
            segment = segment,
            depot = depot,
            maximum = maximum,
            quantity = quantity,
            cost = cost,
            summary = summary,
            fuelLiters = fuelLiters,
            floorValue = floorValue,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted fuel burn estimate in status requested. */
fun newWeightedFuelBurnEstimate(code: String, name: String): WeightedFuelBurnEstimate {
    return WeightedFuelBurnEstimate(
        code = code,
        name = name,
        segment = "",
        depot = "",
        maximum = 0.0,
        quantity = 0,
        cost = 0.0,
        summary = "",
        fuelLiters = 0.0,
        floorValue = 0.0,
        region = "",
        status = WeightedFuelBurnEstimateStatus.REQUESTED,
    )
}
