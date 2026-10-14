package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled locker budget records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a scheduled locker budget is in its life cycle. */
enum class ScheduledLockerBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled locker budget. Its code is unique within a tenant. */
data class ScheduledLockerBudget(
    var code: String,
    var name: String,
    var retries: Int,
    var timezone: String,
    var minimum: Double,
    var driverId: String,
    var contactEmail: String,
    var status: ScheduledLockerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerBudgetStatus): Boolean {
        when (status) {
            ScheduledLockerBudgetStatus.OPEN -> return next == ScheduledLockerBudgetStatus.REOPENED
            ScheduledLockerBudgetStatus.IN_REVIEW -> return next == ScheduledLockerBudgetStatus.RESOLVED
            ScheduledLockerBudgetStatus.RESOLVED -> return next == ScheduledLockerBudgetStatus.REOPENED
            ScheduledLockerBudgetStatus.REOPENED -> return next == ScheduledLockerBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledLockerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled locker budget"} $code"

    fun weightedLoad(): Double = round((minimum + retries.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLockerBudget {
        return ScheduledLockerBudget(
            code = code,
            name = name,
            retries = retries,
            timezone = timezone,
            minimum = minimum,
            driverId = driverId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled locker budget in status open. */
fun newScheduledLockerBudget(code: String, name: String): ScheduledLockerBudget {
    return ScheduledLockerBudget(
        code = code,
        name = name,
        retries = 0,
        timezone = "",
        minimum = 0.0,
        driverId = "",
        contactEmail = "",
        status = ScheduledLockerBudgetStatus.OPEN,
    )
}
