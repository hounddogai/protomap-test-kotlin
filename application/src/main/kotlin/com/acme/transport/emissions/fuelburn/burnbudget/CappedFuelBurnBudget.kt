package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped fuel burn budget records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a capped fuel burn budget is in its life cycle. */
enum class CappedFuelBurnBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped fuel burn budget as the fuel burn team keeps it. */
data class CappedFuelBurnBudget(
    var code: String,
    var title: String,
    var archived: Boolean,
    var timezone: String,
    var pallets: Int,
    var updatedAt: Int,
    var weight: Double,
    var shiftCode: String,
    var status: CappedFuelBurnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedFuelBurnBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedFuelBurnBudgetStatus): Boolean {
        when (status) {
            CappedFuelBurnBudgetStatus.PLANNED -> return next == CappedFuelBurnBudgetStatus.IN_PROGRESS
            CappedFuelBurnBudgetStatus.SCHEDULED -> return next == CappedFuelBurnBudgetStatus.IN_PROGRESS || next == CappedFuelBurnBudgetStatus.COMPLETED
            CappedFuelBurnBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    CappedFuelBurnBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            CappedFuelBurnBudgetStatus.COMPLETED -> {
                when (next) {
                    CappedFuelBurnBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedFuelBurnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("capped fuel burn budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped fuel burn budget"} $code"

    fun weightedTotal(): Double = round(weight * pallets.toDouble() * 100) / 100

    fun snapshot(): CappedFuelBurnBudget {
        return CappedFuelBurnBudget(
            code = code,
            title = title,
            archived = archived,
            timezone = timezone,
            pallets = pallets,
            updatedAt = updatedAt,
            weight = weight,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped fuel burn budget in status planned. */
fun newCappedFuelBurnBudget(code: String, title: String): CappedFuelBurnBudget {
    return CappedFuelBurnBudget(
        code = code,
        title = title,
        archived = false,
        timezone = "",
        pallets = 0,
        updatedAt = 0,
        weight = 0.0,
        shiftCode = "",
        status = CappedFuelBurnBudgetStatus.PLANNED,
    )
}
