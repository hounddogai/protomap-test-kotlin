package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn rollup is in its life cycle. */
enum class FuelBurnRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn rollup and its current status. */
data class FuelBurnRollup(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var score: Double,
    var country: String,
    var distanceKm: Double,
    var externalRef: String,
    var weight: Double,
    var reviewer: String,
    var pallets: Int,
    var status: FuelBurnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnRollupStatus): Boolean {
        when (status) {
            FuelBurnRollupStatus.PLANNED -> return next == FuelBurnRollupStatus.COMPLETED || next == FuelBurnRollupStatus.CANCELLED
            FuelBurnRollupStatus.SCHEDULED -> {
                when (next) {
                    FuelBurnRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FuelBurnRollupStatus.IN_PROGRESS -> return next == FuelBurnRollupStatus.CANCELLED
            FuelBurnRollupStatus.COMPLETED -> {
                when (next) {
                    FuelBurnRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnRollupStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((score - pallets.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnRollup {
        return FuelBurnRollup(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            score = score,
            country = country,
            distanceKm = distanceKm,
            externalRef = externalRef,
            weight = weight,
            reviewer = reviewer,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel burn rollup in status planned. */
fun newFuelBurnRollup(code: String, name: String): FuelBurnRollup {
    return FuelBurnRollup(
        code = code,
        name = name,
        durationMinutes = 0,
        score = 0.0,
        country = "",
        distanceKm = 0.0,
        externalRef = "",
        weight = 0.0,
        reviewer = "",
        pallets = 0,
        status = FuelBurnRollupStatus.PLANNED,
    )
}
