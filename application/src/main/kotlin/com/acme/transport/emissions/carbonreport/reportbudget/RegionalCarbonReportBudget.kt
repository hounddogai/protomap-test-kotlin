package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional carbon report budget is in its life cycle. */
enum class RegionalCarbonReportBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional carbon report budget. Its code is unique within a tenant. */
data class RegionalCarbonReportBudget(
    var code: String,
    var label: String,
    var driverId: String,
    var updatedAt: Int,
    var locale: String,
    var discount: Double,
    var status: RegionalCarbonReportBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportBudgetStatus): Boolean {
        when (status) {
            RegionalCarbonReportBudgetStatus.DRAFT -> {
                when (next) {
                    RegionalCarbonReportBudgetStatus.SUSPENDED, RegionalCarbonReportBudgetStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCarbonReportBudgetStatus.PENDING -> {
                when (next) {
                    RegionalCarbonReportBudgetStatus.ACTIVE, RegionalCarbonReportBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalCarbonReportBudgetStatus.ACTIVE -> return next == RegionalCarbonReportBudgetStatus.CLOSED
            RegionalCarbonReportBudgetStatus.SUSPENDED -> return next == RegionalCarbonReportBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round(updatedAt.toDouble() * discount * 100) / 100

    fun snapshot(): RegionalCarbonReportBudget {
        return RegionalCarbonReportBudget(
            code = code,
            label = label,
            driverId = driverId,
            updatedAt = updatedAt,
            locale = locale,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional carbon report budget in status draft. */
fun newRegionalCarbonReportBudget(code: String, label: String): RegionalCarbonReportBudget {
    return RegionalCarbonReportBudget(
        code = code,
        label = label,
        driverId = "",
        updatedAt = 0,
        locale = "",
        discount = 0.0,
        status = RegionalCarbonReportBudgetStatus.DRAFT,
    )
}
