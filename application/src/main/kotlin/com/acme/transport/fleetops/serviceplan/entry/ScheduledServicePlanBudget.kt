package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.normalize

/** Where a scheduled service plan budget is in its life cycle. */
enum class ScheduledServicePlanBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled service plan budget, with what the planners need to act on it. */
data class ScheduledServicePlanBudget(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var stopCount: Int,
    var region: String,
    var unit: String,
    var status: ScheduledServicePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServicePlanBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServicePlanBudgetStatus): Boolean {
        when (status) {
            ScheduledServicePlanBudgetStatus.OPEN -> return next == ScheduledServicePlanBudgetStatus.IN_REVIEW
            ScheduledServicePlanBudgetStatus.IN_REVIEW -> return next == ScheduledServicePlanBudgetStatus.RESOLVED
            ScheduledServicePlanBudgetStatus.RESOLVED -> {
                when (next) {
                    ScheduledServicePlanBudgetStatus.REOPENED, ScheduledServicePlanBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledServicePlanBudgetStatus.REOPENED -> return next == ScheduledServicePlanBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledServicePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service plan budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled service plan budget"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): ScheduledServicePlanBudget {
        return ScheduledServicePlanBudget(
            code = code,
            name = name,
            tags = tags,
            stopCount = stopCount,
            region = region,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled service plan budget in status open. */
fun newScheduledServicePlanBudget(code: String, name: String): ScheduledServicePlanBudget {
    return ScheduledServicePlanBudget(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        stopCount = 0,
        region = "",
        unit = "",
        status = ScheduledServicePlanBudgetStatus.OPEN,
    )
}
