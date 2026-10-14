package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn batch is in its life cycle. */
enum class FuelBurnBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn batch and its current status. */
data class FuelBurnBatch(
    var code: String,
    var name: String,
    var cost: Double,
    var currency: String,
    var threshold: Double,
    var floorValue: Double,
    var depot: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var reviewer: String,
    var status: FuelBurnBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnBatchStatus): Boolean {
        when (status) {
            FuelBurnBatchStatus.PLANNED -> return next == FuelBurnBatchStatus.SCHEDULED || next == FuelBurnBatchStatus.COMPLETED
            FuelBurnBatchStatus.SCHEDULED -> {
                when (next) {
                    FuelBurnBatchStatus.COMPLETED -> return true
                }
                return false
            }
            FuelBurnBatchStatus.IN_PROGRESS -> {
                when (next) {
                    FuelBurnBatchStatus.COMPLETED, FuelBurnBatchStatus.CANCELLED -> return true
                }
                return false
            }
            FuelBurnBatchStatus.COMPLETED -> return next == FuelBurnBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelBurnBatchStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(cost * floorValue * 100) / 100

    fun snapshot(): FuelBurnBatch {
        return FuelBurnBatch(
            code = code,
            name = name,
            cost = cost,
            currency = currency,
            threshold = threshold,
            floorValue = floorValue,
            depot = depot,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel burn batch in status planned. */
fun newFuelBurnBatch(code: String, name: String): FuelBurnBatch {
    return FuelBurnBatch(
        code = code,
        name = name,
        cost = 0.0,
        currency = "",
        threshold = 0.0,
        floorValue = 0.0,
        depot = "",
        fuelLiters = 0.0,
        loadKg = 0.0,
        reviewer = "",
        status = FuelBurnBatchStatus.PLANNED,
    )
}
