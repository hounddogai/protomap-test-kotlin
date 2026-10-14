package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.normalize

/** Where a capped utilization budget is in its life cycle. */
enum class CappedUtilizationBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped utilization budget, with what the planners need to act on it. */
data class CappedUtilizationBudget(
    var code: String,
    var name: String,
    var quantity: Int,
    var category: String,
    var trailerId: String,
    var status: CappedUtilizationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedUtilizationBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedUtilizationBudgetStatus): Boolean {
        when (status) {
            CappedUtilizationBudgetStatus.IDLE -> {
                when (next) {
                    CappedUtilizationBudgetStatus.READY -> return true
                }
                return false
            }
            CappedUtilizationBudgetStatus.WARMING -> return next == CappedUtilizationBudgetStatus.READY
            CappedUtilizationBudgetStatus.READY -> return next == CappedUtilizationBudgetStatus.DRAINING || next == CappedUtilizationBudgetStatus.STOPPED
            CappedUtilizationBudgetStatus.DRAINING -> {
                when (next) {
                    CappedUtilizationBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedUtilizationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("capped utilization budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CappedUtilizationBudget {
        return CappedUtilizationBudget(
            code = code,
            name = name,
            quantity = quantity,
            category = category,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped utilization budget in status idle. */
fun newCappedUtilizationBudget(code: String, name: String): CappedUtilizationBudget {
    return CappedUtilizationBudget(
        code = code,
        name = name,
        quantity = 0,
        category = "",
        trailerId = "",
        status = CappedUtilizationBudgetStatus.IDLE,
    )
}
