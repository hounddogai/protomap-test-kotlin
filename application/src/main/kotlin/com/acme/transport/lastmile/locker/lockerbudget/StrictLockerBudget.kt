package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict locker budget records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a strict locker budget is in its life cycle. */
enum class StrictLockerBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict locker budget as the locker team keeps it. */
data class StrictLockerBudget(
    var code: String,
    var name: String,
    var reviewer: String,
    var attempts: Int,
    var amount: Double,
    var endsAt: Int,
    var quantity: Int,
    var archived: Boolean,
    var graceDays: Int,
    var limit: Int,
    var status: StrictLockerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLockerBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLockerBudgetStatus): Boolean {
        when (status) {
            StrictLockerBudgetStatus.CREATED -> return next == StrictLockerBudgetStatus.PUBLISHED
            StrictLockerBudgetStatus.VALIDATED -> return next == StrictLockerBudgetStatus.PUBLISHED || next == StrictLockerBudgetStatus.RETIRED
            StrictLockerBudgetStatus.PUBLISHED -> {
                when (next) {
                    StrictLockerBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictLockerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("strict locker budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round(limit.toDouble() * amount * 100) / 100

    fun snapshot(): StrictLockerBudget {
        return StrictLockerBudget(
            code = code,
            name = name,
            reviewer = reviewer,
            attempts = attempts,
            amount = amount,
            endsAt = endsAt,
            quantity = quantity,
            archived = archived,
            graceDays = graceDays,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict locker budget in status created. */
fun newStrictLockerBudget(code: String, name: String): StrictLockerBudget {
    return StrictLockerBudget(
        code = code,
        name = name,
        reviewer = "",
        attempts = 0,
        amount = 0.0,
        endsAt = 0,
        quantity = 0,
        archived = false,
        graceDays = 0,
        limit = 0,
        status = StrictLockerBudgetStatus.CREATED,
    )
}
