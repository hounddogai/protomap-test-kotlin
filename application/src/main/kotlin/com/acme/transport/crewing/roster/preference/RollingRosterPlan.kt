package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize

/** Where a rolling roster plan is in its life cycle. */
enum class RollingRosterPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling roster plan. Its code is unique within a tenant. */
data class RollingRosterPlan(
    var code: String,
    var title: String,
    var ownerId: String,
    var lane: String,
    var graceDays: Int,
    var status: RollingRosterPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRosterPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRosterPlanStatus): Boolean {
        when (status) {
            RollingRosterPlanStatus.CREATED -> {
                when (next) {
                    RollingRosterPlanStatus.VALIDATED -> return true
                }
                return false
            }
            RollingRosterPlanStatus.VALIDATED -> return next == RollingRosterPlanStatus.RETIRED
            RollingRosterPlanStatus.PUBLISHED -> return next == RollingRosterPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingRosterPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling roster plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling roster plan"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): RollingRosterPlan {
        return RollingRosterPlan(
            code = code,
            title = title,
            ownerId = ownerId,
            lane = lane,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling roster plan in status created. */
fun newRollingRosterPlan(code: String, title: String): RollingRosterPlan {
    return RollingRosterPlan(
        code = code,
        title = title,
        ownerId = "",
        lane = "",
        graceDays = 0,
        status = RollingRosterPlanStatus.CREATED,
    )
}
