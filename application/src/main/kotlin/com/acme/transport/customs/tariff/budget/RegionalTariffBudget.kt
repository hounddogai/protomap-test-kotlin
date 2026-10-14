package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional tariff budget records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a regional tariff budget is in its life cycle. */
enum class RegionalTariffBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional tariff budget as the tariff team keeps it. */
data class RegionalTariffBudget(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var amount: Double,
    var createdAt: Int,
    var margin: Double,
    var fuelLiters: Double,
    var revision: Int,
    var status: RegionalTariffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTariffBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTariffBudgetStatus): Boolean {
        when (status) {
            RegionalTariffBudgetStatus.REQUESTED -> {
                when (next) {
                    RegionalTariffBudgetStatus.GRANTED, RegionalTariffBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalTariffBudgetStatus.GRANTED -> return next == RegionalTariffBudgetStatus.REVOKED || next == RegionalTariffBudgetStatus.EXPIRED
            RegionalTariffBudgetStatus.REVOKED -> {
                when (next) {
                    RegionalTariffBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTariffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional tariff budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"regional tariff budget"} $code"

    fun projectedLoad(): Double = round((distanceKm - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalTariffBudget {
        return RegionalTariffBudget(
            code = code,
            label = label,
            distanceKm = distanceKm,
            amount = amount,
            createdAt = createdAt,
            margin = margin,
            fuelLiters = fuelLiters,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional tariff budget in status requested. */
fun newRegionalTariffBudget(code: String, label: String): RegionalTariffBudget {
    return RegionalTariffBudget(
        code = code,
        label = label,
        distanceKm = 0.0,
        amount = 0.0,
        createdAt = 0,
        margin = 0.0,
        fuelLiters = 0.0,
        revision = 0,
        status = RegionalTariffBudgetStatus.REQUESTED,
    )
}
