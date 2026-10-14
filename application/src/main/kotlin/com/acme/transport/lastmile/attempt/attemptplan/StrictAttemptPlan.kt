package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.text.normalize

// The strict attempt plan records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a strict attempt plan is in its life cycle. */
enum class StrictAttemptPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict attempt plan. Its code is unique within a tenant. */
data class StrictAttemptPlan(
    var code: String,
    var title: String,
    var depot: String,
    var visible: Boolean,
    var currency: String,
    var quantity: Int,
    var status: StrictAttemptPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptPlanStatus): Boolean {
        when (status) {
            StrictAttemptPlanStatus.CREATED -> {
                when (next) {
                    StrictAttemptPlanStatus.VALIDATED, StrictAttemptPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictAttemptPlanStatus.VALIDATED -> {
                when (next) {
                    StrictAttemptPlanStatus.RETIRED -> return true
                }
                return false
            }
            StrictAttemptPlanStatus.PUBLISHED -> {
                when (next) {
                    StrictAttemptPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): StrictAttemptPlan {
        return StrictAttemptPlan(
            code = code,
            title = title,
            depot = depot,
            visible = visible,
            currency = currency,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict attempt plan in status created. */
fun newStrictAttemptPlan(code: String, title: String): StrictAttemptPlan {
    return StrictAttemptPlan(
        code = code,
        title = title,
        depot = "",
        visible = false,
        currency = "",
        quantity = 0,
        status = StrictAttemptPlanStatus.CREATED,
    )
}
