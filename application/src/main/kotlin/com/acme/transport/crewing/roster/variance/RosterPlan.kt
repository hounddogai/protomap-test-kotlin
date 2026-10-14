package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster plan is in its life cycle. */
enum class RosterPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One roster plan, with what the planners need to act on it. */
data class RosterPlan(
    var code: String,
    var title: String,
    var label: String,
    var priority: Int,
    var timezone: String,
    var fuelLiters: Double,
    var source: String,
    var status: RosterPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterPlanStatus): Boolean {
        when (status) {
            RosterPlanStatus.CREATED -> {
                when (next) {
                    RosterPlanStatus.VALIDATED, RosterPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            RosterPlanStatus.VALIDATED -> return next == RosterPlanStatus.RETIRED
            RosterPlanStatus.PUBLISHED -> {
                when (next) {
                    RosterPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterPlanStatus) {
        if (!canMoveTo(next)) {
            error("roster plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedTotal(): Double = round((priority.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): RosterPlan {
        return RosterPlan(
            code = code,
            title = title,
            label = label,
            priority = priority,
            timezone = timezone,
            fuelLiters = fuelLiters,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a roster plan in status created. */
fun newRosterPlan(code: String, title: String): RosterPlan {
    return RosterPlan(
        code = code,
        title = title,
        label = "",
        priority = 0,
        timezone = "",
        fuelLiters = 0.0,
        source = "",
        status = RosterPlanStatus.CREATED,
    )
}
