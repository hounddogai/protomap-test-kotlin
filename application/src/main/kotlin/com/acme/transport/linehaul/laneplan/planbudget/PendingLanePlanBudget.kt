package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending lane plan budget records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a pending lane plan budget is in its life cycle. */
enum class PendingLanePlanBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending lane plan budget as the lane plan team keeps it. */
data class PendingLanePlanBudget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var timezone: String,
    var weight: Double,
    var maximum: Double,
    var title: String,
    var graceDays: Int,
    var status: PendingLanePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLanePlanBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLanePlanBudgetStatus): Boolean {
        when (status) {
            PendingLanePlanBudgetStatus.OPEN -> return next == PendingLanePlanBudgetStatus.RESOLVED || next == PendingLanePlanBudgetStatus.REOPENED
            PendingLanePlanBudgetStatus.IN_REVIEW -> return next == PendingLanePlanBudgetStatus.ARCHIVED
            PendingLanePlanBudgetStatus.RESOLVED -> {
                when (next) {
                    PendingLanePlanBudgetStatus.REOPENED -> return true
                }
                return false
            }
            PendingLanePlanBudgetStatus.REOPENED -> return next == PendingLanePlanBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingLanePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending lane plan budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round(maximum * weight * 100) / 100

    fun snapshot(): PendingLanePlanBudget {
        return PendingLanePlanBudget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            timezone = timezone,
            weight = weight,
            maximum = maximum,
            title = title,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending lane plan budget in status open. */
fun newPendingLanePlanBudget(code: String, name: String): PendingLanePlanBudget {
    return PendingLanePlanBudget(
        code = code,
        name = name,
        etaMinutes = 0,
        timezone = "",
        weight = 0.0,
        maximum = 0.0,
        title = "",
        graceDays = 0,
        status = PendingLanePlanBudgetStatus.OPEN,
    )
}
