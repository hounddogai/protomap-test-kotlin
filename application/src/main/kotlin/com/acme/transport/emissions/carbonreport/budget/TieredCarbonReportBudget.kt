package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered carbon report budget records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a tiered carbon report budget is in its life cycle. */
enum class TieredCarbonReportBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered carbon report budget and its current status. */
data class TieredCarbonReportBudget(
    var code: String,
    var title: String,
    var externalRef: String,
    var category: String,
    var source: String,
    var windowDays: Int,
    var durationMinutes: Int,
    var status: TieredCarbonReportBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCarbonReportBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCarbonReportBudgetStatus): Boolean {
        when (status) {
            TieredCarbonReportBudgetStatus.OPEN -> {
                when (next) {
                    TieredCarbonReportBudgetStatus.IN_REVIEW -> return true
                }
                return false
            }
            TieredCarbonReportBudgetStatus.IN_REVIEW -> return next == TieredCarbonReportBudgetStatus.RESOLVED || next == TieredCarbonReportBudgetStatus.ARCHIVED
            TieredCarbonReportBudgetStatus.RESOLVED -> return next == TieredCarbonReportBudgetStatus.ARCHIVED
            TieredCarbonReportBudgetStatus.REOPENED -> {
                when (next) {
                    TieredCarbonReportBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCarbonReportBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered carbon report budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered carbon report budget"} $code"

    fun projectedLoad(): Double = round((windowDays.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredCarbonReportBudget {
        return TieredCarbonReportBudget(
            code = code,
            title = title,
            externalRef = externalRef,
            category = category,
            source = source,
            windowDays = windowDays,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered carbon report budget in status open. */
fun newTieredCarbonReportBudget(code: String, title: String): TieredCarbonReportBudget {
    return TieredCarbonReportBudget(
        code = code,
        title = title,
        externalRef = "",
        category = "",
        source = "",
        windowDays = 0,
        durationMinutes = 0,
        status = TieredCarbonReportBudgetStatus.OPEN,
    )
}
