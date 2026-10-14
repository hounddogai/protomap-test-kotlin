package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered fuel burn rollup is in its life cycle. */
enum class TieredFuelBurnRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered fuel burn rollup as the fuel burn team keeps it. */
data class TieredFuelBurnRollup(
    var code: String,
    var label: String,
    var lane: String,
    var revision: Int,
    var vehicleId: String,
    var margin: Double,
    var carrier: String,
    var pallets: Int,
    var batchSize: Int,
    var country: String,
    var budget: Double,
    var status: TieredFuelBurnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnRollupStatus): Boolean {
        when (status) {
            TieredFuelBurnRollupStatus.REQUESTED -> return next == TieredFuelBurnRollupStatus.GRANTED || next == TieredFuelBurnRollupStatus.REVOKED
            TieredFuelBurnRollupStatus.GRANTED -> {
                when (next) {
                    TieredFuelBurnRollupStatus.REVOKED -> return true
                }
                return false
            }
            TieredFuelBurnRollupStatus.REVOKED -> return next == TieredFuelBurnRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((revision.toDouble() - budget) * 100) / 100

    fun snapshot(): TieredFuelBurnRollup {
        return TieredFuelBurnRollup(
            code = code,
            label = label,
            lane = lane,
            revision = revision,
            vehicleId = vehicleId,
            margin = margin,
            carrier = carrier,
            pallets = pallets,
            batchSize = batchSize,
            country = country,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered fuel burn rollup in status requested. */
fun newTieredFuelBurnRollup(code: String, label: String): TieredFuelBurnRollup {
    return TieredFuelBurnRollup(
        code = code,
        label = label,
        lane = "",
        revision = 0,
        vehicleId = "",
        margin = 0.0,
        carrier = "",
        pallets = 0,
        batchSize = 0,
        country = "",
        budget = 0.0,
        status = TieredFuelBurnRollupStatus.REQUESTED,
    )
}
