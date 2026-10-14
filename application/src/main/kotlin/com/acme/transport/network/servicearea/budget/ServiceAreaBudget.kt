package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.text.normalize

// The service area budget records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area budget is in its life cycle. */
enum class ServiceAreaBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One service area budget, with what the planners need to act on it. */
data class ServiceAreaBudget(
    var code: String,
    var label: String,
    var retries: Int,
    var description: String,
    var trailerId: String,
    var status: ServiceAreaBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaBudgetStatus): Boolean {
        when (status) {
            ServiceAreaBudgetStatus.DRAFT -> return next == ServiceAreaBudgetStatus.CLOSED
            ServiceAreaBudgetStatus.PENDING -> return next == ServiceAreaBudgetStatus.SUSPENDED
            ServiceAreaBudgetStatus.ACTIVE -> return next == ServiceAreaBudgetStatus.SUSPENDED
            ServiceAreaBudgetStatus.SUSPENDED -> return next == ServiceAreaBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ServiceAreaBudgetStatus) {
        if (!canMoveTo(next)) {
            error("service area budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): ServiceAreaBudget {
        return ServiceAreaBudget(
            code = code,
            label = label,
            retries = retries,
            description = description,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area budget in status draft. */
fun newServiceAreaBudget(code: String, label: String): ServiceAreaBudget {
    return ServiceAreaBudget(
        code = code,
        label = label,
        retries = 0,
        description = "",
        trailerId = "",
        status = ServiceAreaBudgetStatus.DRAFT,
    )
}
