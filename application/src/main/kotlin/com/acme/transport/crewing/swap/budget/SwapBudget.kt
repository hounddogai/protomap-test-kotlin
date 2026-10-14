package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.text.normalize

// The swap budget records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap budget is in its life cycle. */
enum class SwapBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A swap budget. Its code is unique within a tenant. */
data class SwapBudget(
    var code: String,
    var name: String,
    var timezone: String,
    var depot: String,
    var rate: Double,
    var status: SwapBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapBudgetStatus): Boolean {
        when (status) {
            SwapBudgetStatus.OPEN -> return next == SwapBudgetStatus.REOPENED
            SwapBudgetStatus.IN_REVIEW -> return next == SwapBudgetStatus.ARCHIVED
            SwapBudgetStatus.RESOLVED -> {
                when (next) {
                    SwapBudgetStatus.REOPENED -> return true
                }
                return false
            }
            SwapBudgetStatus.REOPENED -> {
                when (next) {
                    SwapBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapBudgetStatus) {
        if (!canMoveTo(next)) {
            error("swap budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = rate

    fun snapshot(): SwapBudget {
        return SwapBudget(
            code = code,
            name = name,
            timezone = timezone,
            depot = depot,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a swap budget in status open. */
fun newSwapBudget(code: String, name: String): SwapBudget {
    return SwapBudget(
        code = code,
        name = name,
        timezone = "",
        depot = "",
        rate = 0.0,
        status = SwapBudgetStatus.OPEN,
    )
}
