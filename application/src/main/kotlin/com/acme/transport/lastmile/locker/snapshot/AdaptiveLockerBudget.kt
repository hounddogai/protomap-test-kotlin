package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize

/** Where a adaptive locker budget is in its life cycle. */
enum class AdaptiveLockerBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive locker budget as the locker team keeps it. */
data class AdaptiveLockerBudget(
    var code: String,
    var label: String,
    var routeCode: String,
    var source: String,
    var reviewer: String,
    var amount: Double,
    var archived: Boolean,
    var approved: Boolean,
    var status: AdaptiveLockerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerBudgetStatus): Boolean {
        when (status) {
            AdaptiveLockerBudgetStatus.CREATED -> return next == AdaptiveLockerBudgetStatus.RETIRED
            AdaptiveLockerBudgetStatus.VALIDATED -> {
                when (next) {
                    AdaptiveLockerBudgetStatus.PUBLISHED, AdaptiveLockerBudgetStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveLockerBudgetStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveLockerBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive locker budget"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): AdaptiveLockerBudget {
        return AdaptiveLockerBudget(
            code = code,
            label = label,
            routeCode = routeCode,
            source = source,
            reviewer = reviewer,
            amount = amount,
            archived = archived,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive locker budget in status created. */
fun newAdaptiveLockerBudget(code: String, label: String): AdaptiveLockerBudget {
    return AdaptiveLockerBudget(
        code = code,
        label = label,
        routeCode = "",
        source = "",
        reviewer = "",
        amount = 0.0,
        archived = false,
        approved = false,
        status = AdaptiveLockerBudgetStatus.CREATED,
    )
}
