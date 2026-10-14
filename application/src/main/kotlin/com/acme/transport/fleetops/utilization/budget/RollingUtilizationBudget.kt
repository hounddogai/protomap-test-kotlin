package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling utilization budget records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a rolling utilization budget is in its life cycle. */
enum class RollingUtilizationBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling utilization budget and its current status. */
data class RollingUtilizationBudget(
    var code: String,
    var title: String,
    var label: String,
    var amount: Double,
    var fuelLiters: Double,
    var lane: String,
    var unit: String,
    var odometer: Int,
    var status: RollingUtilizationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingUtilizationBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingUtilizationBudgetStatus): Boolean {
        when (status) {
            RollingUtilizationBudgetStatus.OPEN -> {
                when (next) {
                    RollingUtilizationBudgetStatus.RESOLVED, RollingUtilizationBudgetStatus.REOPENED -> return true
                }
                return false
            }
            RollingUtilizationBudgetStatus.IN_REVIEW -> return next == RollingUtilizationBudgetStatus.RESOLVED || next == RollingUtilizationBudgetStatus.ARCHIVED
            RollingUtilizationBudgetStatus.RESOLVED -> {
                when (next) {
                    RollingUtilizationBudgetStatus.REOPENED, RollingUtilizationBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingUtilizationBudgetStatus.REOPENED -> {
                when (next) {
                    RollingUtilizationBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingUtilizationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rolling utilization budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun netValue(): Double = round(fuelLiters * amount * 100) / 100

    fun snapshot(): RollingUtilizationBudget {
        return RollingUtilizationBudget(
            code = code,
            title = title,
            label = label,
            amount = amount,
            fuelLiters = fuelLiters,
            lane = lane,
            unit = unit,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling utilization budget in status open. */
fun newRollingUtilizationBudget(code: String, title: String): RollingUtilizationBudget {
    return RollingUtilizationBudget(
        code = code,
        title = title,
        label = "",
        amount = 0.0,
        fuelLiters = 0.0,
        lane = "",
        unit = "",
        odometer = 0,
        status = RollingUtilizationBudgetStatus.OPEN,
    )
}
