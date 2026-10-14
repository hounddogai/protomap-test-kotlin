package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback carbon report budget records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a fallback carbon report budget is in its life cycle. */
enum class FallbackCarbonReportBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback carbon report budget. Its code is unique within a tenant. */
data class FallbackCarbonReportBudget(
    var code: String,
    var name: String,
    var currency: String,
    var depot: String,
    var retries: Int,
    var threshold: Double,
    var status: FallbackCarbonReportBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportBudgetStatus): Boolean {
        when (status) {
            FallbackCarbonReportBudgetStatus.REQUESTED -> {
                when (next) {
                    FallbackCarbonReportBudgetStatus.GRANTED, FallbackCarbonReportBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackCarbonReportBudgetStatus.GRANTED -> return next == FallbackCarbonReportBudgetStatus.EXPIRED
            FallbackCarbonReportBudgetStatus.REVOKED -> {
                when (next) {
                    FallbackCarbonReportBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((retries.toDouble() + threshold) * 100) / 100

    fun snapshot(): FallbackCarbonReportBudget {
        return FallbackCarbonReportBudget(
            code = code,
            name = name,
            currency = currency,
            depot = depot,
            retries = retries,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback carbon report budget in status requested. */
fun newFallbackCarbonReportBudget(code: String, name: String): FallbackCarbonReportBudget {
    return FallbackCarbonReportBudget(
        code = code,
        name = name,
        currency = "",
        depot = "",
        retries = 0,
        threshold = 0.0,
        status = FallbackCarbonReportBudgetStatus.REQUESTED,
    )
}
