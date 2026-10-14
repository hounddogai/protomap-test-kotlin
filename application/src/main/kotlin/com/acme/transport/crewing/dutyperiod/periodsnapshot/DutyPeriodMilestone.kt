package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.text.normalize

/** Where a duty period milestone is in its life cycle. */
enum class DutyPeriodMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A duty period milestone as the duty period team keeps it. */
data class DutyPeriodMilestone(
    var code: String,
    var title: String,
    var graceDays: Int,
    var name: String,
    var routeCode: String,
    var status: DutyPeriodMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodMilestoneStatus): Boolean {
        when (status) {
            DutyPeriodMilestoneStatus.OPEN -> return next == DutyPeriodMilestoneStatus.IN_REVIEW
            DutyPeriodMilestoneStatus.IN_REVIEW -> return next == DutyPeriodMilestoneStatus.REOPENED || next == DutyPeriodMilestoneStatus.ARCHIVED
            DutyPeriodMilestoneStatus.RESOLVED -> {
                when (next) {
                    DutyPeriodMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            DutyPeriodMilestoneStatus.REOPENED -> return next == DutyPeriodMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DutyPeriodMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("duty period milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"duty period milestone"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): DutyPeriodMilestone {
        return DutyPeriodMilestone(
            code = code,
            title = title,
            graceDays = graceDays,
            name = name,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period milestone in status open. */
fun newDutyPeriodMilestone(code: String, title: String): DutyPeriodMilestone {
    return DutyPeriodMilestone(
        code = code,
        title = title,
        graceDays = 0,
        name = "",
        routeCode = "",
        status = DutyPeriodMilestoneStatus.OPEN,
    )
}
