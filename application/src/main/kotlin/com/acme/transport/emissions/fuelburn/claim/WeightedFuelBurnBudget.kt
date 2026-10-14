package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel burn budget is in its life cycle. */
enum class WeightedFuelBurnBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted fuel burn budget. Its code is unique within a tenant. */
data class WeightedFuelBurnBudget(
    var code: String,
    var name: String,
    var ceiling: Double,
    var active: Boolean,
    var weight: Double,
    var locale: String,
    var source: String,
    var odometer: Int,
    var status: WeightedFuelBurnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnBudgetStatus): Boolean {
        when (status) {
            WeightedFuelBurnBudgetStatus.PLANNED -> return next == WeightedFuelBurnBudgetStatus.SCHEDULED || next == WeightedFuelBurnBudgetStatus.COMPLETED
            WeightedFuelBurnBudgetStatus.SCHEDULED -> return next == WeightedFuelBurnBudgetStatus.COMPLETED
            WeightedFuelBurnBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedFuelBurnBudgetStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedFuelBurnBudgetStatus.COMPLETED -> return next == WeightedFuelBurnBudgetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round(weight * odometer.toDouble() * 100) / 100

    fun snapshot(): WeightedFuelBurnBudget {
        return WeightedFuelBurnBudget(
            code = code,
            name = name,
            ceiling = ceiling,
            active = active,
            weight = weight,
            locale = locale,
            source = source,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted fuel burn budget in status planned. */
fun newWeightedFuelBurnBudget(code: String, name: String): WeightedFuelBurnBudget {
    return WeightedFuelBurnBudget(
        code = code,
        name = name,
        ceiling = 0.0,
        active = false,
        weight = 0.0,
        locale = "",
        source = "",
        odometer = 0,
        status = WeightedFuelBurnBudgetStatus.PLANNED,
    )
}
