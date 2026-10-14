package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn budget records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn budget is in its life cycle. */
enum class FuelBurnBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fuel burn budget and its current status. */
data class FuelBurnBudget(
    var code: String,
    var title: String,
    var region: String,
    var sequence: Int,
    var priority: Int,
    var description: String,
    var updatedAt: Int,
    var status: FuelBurnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnBudgetStatus): Boolean {
        when (status) {
            FuelBurnBudgetStatus.REQUESTED -> return next == FuelBurnBudgetStatus.GRANTED || next == FuelBurnBudgetStatus.REVOKED
            FuelBurnBudgetStatus.GRANTED -> return next == FuelBurnBudgetStatus.REVOKED
            FuelBurnBudgetStatus.REVOKED -> {
                when (next) {
                    FuelBurnBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(updatedAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): FuelBurnBudget {
        return FuelBurnBudget(
            code = code,
            title = title,
            region = region,
            sequence = sequence,
            priority = priority,
            description = description,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel burn budget in status requested. */
fun newFuelBurnBudget(code: String, title: String): FuelBurnBudget {
    return FuelBurnBudget(
        code = code,
        title = title,
        region = "",
        sequence = 0,
        priority = 0,
        description = "",
        updatedAt = 0,
        status = FuelBurnBudgetStatus.REQUESTED,
    )
}
