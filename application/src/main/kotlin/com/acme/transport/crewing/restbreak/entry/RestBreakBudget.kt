package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break budget is in its life cycle. */
enum class RestBreakBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rest break budget, with what the planners need to act on it. */
data class RestBreakBudget(
    var code: String,
    var name: String,
    var title: String,
    var routeCode: String,
    var weight: Double,
    var checksum: String,
    var category: String,
    var unit: String,
    var quantity: Int,
    var amount: Double,
    var status: RestBreakBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakBudgetStatus): Boolean {
        when (status) {
            RestBreakBudgetStatus.REQUESTED -> return next == RestBreakBudgetStatus.EXPIRED
            RestBreakBudgetStatus.GRANTED -> return next == RestBreakBudgetStatus.EXPIRED
            RestBreakBudgetStatus.REVOKED -> return next == RestBreakBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RestBreakBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rest break budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rest break budget"} $code"

    fun adjustedTotal(): Double = round(quantity.toDouble() * weight * 100) / 100

    fun snapshot(): RestBreakBudget {
        return RestBreakBudget(
            code = code,
            name = name,
            title = title,
            routeCode = routeCode,
            weight = weight,
            checksum = checksum,
            category = category,
            unit = unit,
            quantity = quantity,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rest break budget in status requested. */
fun newRestBreakBudget(code: String, name: String): RestBreakBudget {
    return RestBreakBudget(
        code = code,
        name = name,
        title = "",
        routeCode = "",
        weight = 0.0,
        checksum = "",
        category = "",
        unit = "",
        quantity = 0,
        amount = 0.0,
        status = RestBreakBudgetStatus.REQUESTED,
    )
}
