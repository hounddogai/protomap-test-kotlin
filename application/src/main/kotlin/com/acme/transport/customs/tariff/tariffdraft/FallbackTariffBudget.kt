package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.text.normalize

/** Where a fallback tariff budget is in its life cycle. */
enum class FallbackTariffBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fallback tariff budget and its current status. */
data class FallbackTariffBudget(
    var code: String,
    var label: String,
    var notes: String,
    var description: String,
    var country: String,
    var timezone: String,
    var channel: String,
    var trailerId: String,
    var quantity: Int,
    var status: FallbackTariffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffBudgetStatus): Boolean {
        when (status) {
            FallbackTariffBudgetStatus.PLANNED -> {
                when (next) {
                    FallbackTariffBudgetStatus.SCHEDULED, FallbackTariffBudgetStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FallbackTariffBudgetStatus.SCHEDULED -> return next == FallbackTariffBudgetStatus.IN_PROGRESS || next == FallbackTariffBudgetStatus.COMPLETED
            FallbackTariffBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackTariffBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackTariffBudgetStatus.COMPLETED -> {
                when (next) {
                    FallbackTariffBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTariffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback tariff budget"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): FallbackTariffBudget {
        return FallbackTariffBudget(
            code = code,
            label = label,
            notes = notes,
            description = description,
            country = country,
            timezone = timezone,
            channel = channel,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback tariff budget in status planned. */
fun newFallbackTariffBudget(code: String, label: String): FallbackTariffBudget {
    return FallbackTariffBudget(
        code = code,
        label = label,
        notes = "",
        description = "",
        country = "",
        timezone = "",
        channel = "",
        trailerId = "",
        quantity = 0,
        status = FallbackTariffBudgetStatus.PLANNED,
    )
}
