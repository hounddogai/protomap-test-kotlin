package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary route offset budget records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a primary route offset budget is in its life cycle. */
enum class PrimaryRouteOffsetBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary route offset budget as the route offset team keeps it. */
data class PrimaryRouteOffsetBudget(
    var code: String,
    var label: String,
    var country: String,
    var loadKg: Double,
    var shiftCode: String,
    var graceDays: Int,
    var limit: Int,
    var category: String,
    var status: PrimaryRouteOffsetBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRouteOffsetBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRouteOffsetBudgetStatus): Boolean {
        when (status) {
            PrimaryRouteOffsetBudgetStatus.CREATED -> {
                when (next) {
                    PrimaryRouteOffsetBudgetStatus.VALIDATED, PrimaryRouteOffsetBudgetStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryRouteOffsetBudgetStatus.VALIDATED -> {
                when (next) {
                    PrimaryRouteOffsetBudgetStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryRouteOffsetBudgetStatus.PUBLISHED -> return next == PrimaryRouteOffsetBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryRouteOffsetBudgetStatus) {
        if (!canMoveTo(next)) {
            error("primary route offset budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary route offset budget"} $code"

    fun projectedTotal(): Double = round((graceDays.toDouble() + loadKg) * 100) / 100

    fun snapshot(): PrimaryRouteOffsetBudget {
        return PrimaryRouteOffsetBudget(
            code = code,
            label = label,
            country = country,
            loadKg = loadKg,
            shiftCode = shiftCode,
            graceDays = graceDays,
            limit = limit,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary route offset budget in status created. */
fun newPrimaryRouteOffsetBudget(code: String, label: String): PrimaryRouteOffsetBudget {
    return PrimaryRouteOffsetBudget(
        code = code,
        label = label,
        country = "",
        loadKg = 0.0,
        shiftCode = "",
        graceDays = 0,
        limit = 0,
        category = "",
        status = PrimaryRouteOffsetBudgetStatus.CREATED,
    )
}
