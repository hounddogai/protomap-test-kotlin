package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor budget is in its life cycle. */
enum class EmissionFactorBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A emission factor budget as the emission factor team keeps it. */
data class EmissionFactorBudget(
    var code: String,
    var name: String,
    var priority: Int,
    var rate: Double,
    var ratio: Double,
    var status: EmissionFactorBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorBudgetStatus): Boolean {
        when (status) {
            EmissionFactorBudgetStatus.PLANNED -> return next == EmissionFactorBudgetStatus.SCHEDULED
            EmissionFactorBudgetStatus.SCHEDULED -> {
                when (next) {
                    EmissionFactorBudgetStatus.COMPLETED, EmissionFactorBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            EmissionFactorBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    EmissionFactorBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            EmissionFactorBudgetStatus.COMPLETED -> return next == EmissionFactorBudgetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: EmissionFactorBudgetStatus) {
        if (!canMoveTo(next)) {
            error("emission factor budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(priority.toDouble() * rate * 100) / 100

    fun snapshot(): EmissionFactorBudget {
        return EmissionFactorBudget(
            code = code,
            name = name,
            priority = priority,
            rate = rate,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a emission factor budget in status planned. */
fun newEmissionFactorBudget(code: String, name: String): EmissionFactorBudget {
    return EmissionFactorBudget(
        code = code,
        name = name,
        priority = 0,
        rate = 0.0,
        ratio = 0.0,
        status = EmissionFactorBudgetStatus.PLANNED,
    )
}
