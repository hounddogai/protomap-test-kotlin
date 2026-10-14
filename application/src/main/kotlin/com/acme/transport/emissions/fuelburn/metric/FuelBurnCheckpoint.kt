package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn checkpoint is in its life cycle. */
enum class FuelBurnCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel burn checkpoint. Its code is unique within a tenant. */
data class FuelBurnCheckpoint(
    var code: String,
    var name: String,
    var score: Double,
    var expiresAt: Int,
    var updatedAt: Int,
    var priority: Int,
    var category: String,
    var amount: Double,
    var discount: Double,
    var distanceKm: Double,
    var status: FuelBurnCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnCheckpointStatus): Boolean {
        when (status) {
            FuelBurnCheckpointStatus.CREATED -> return next == FuelBurnCheckpointStatus.VALIDATED
            FuelBurnCheckpointStatus.VALIDATED -> {
                when (next) {
                    FuelBurnCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            FuelBurnCheckpointStatus.PUBLISHED -> return next == FuelBurnCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round((discount - distanceKm) * 100) / 100

    fun snapshot(): FuelBurnCheckpoint {
        return FuelBurnCheckpoint(
            code = code,
            name = name,
            score = score,
            expiresAt = expiresAt,
            updatedAt = updatedAt,
            priority = priority,
            category = category,
            amount = amount,
            discount = discount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fuel burn checkpoint in status created. */
fun newFuelBurnCheckpoint(code: String, name: String): FuelBurnCheckpoint {
    return FuelBurnCheckpoint(
        code = code,
        name = name,
        score = 0.0,
        expiresAt = 0,
        updatedAt = 0,
        priority = 0,
        category = "",
        amount = 0.0,
        discount = 0.0,
        distanceKm = 0.0,
        status = FuelBurnCheckpointStatus.CREATED,
    )
}
