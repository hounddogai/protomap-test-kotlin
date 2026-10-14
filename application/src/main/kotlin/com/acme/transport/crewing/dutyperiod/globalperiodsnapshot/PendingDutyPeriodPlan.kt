package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending duty period plan is in its life cycle. */
enum class PendingDutyPeriodPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending duty period plan and its current status. */
data class PendingDutyPeriodPlan(
    var code: String,
    var name: String,
    var cost: Double,
    var revision: Int,
    var timezone: String,
    var status: PendingDutyPeriodPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodPlanStatus): Boolean {
        when (status) {
            PendingDutyPeriodPlanStatus.CREATED -> return next == PendingDutyPeriodPlanStatus.RETIRED
            PendingDutyPeriodPlanStatus.VALIDATED -> return next == PendingDutyPeriodPlanStatus.PUBLISHED
            PendingDutyPeriodPlanStatus.PUBLISHED -> return next == PendingDutyPeriodPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodPlanStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"pending duty period plan"} $code"

    fun projectedValue(): Double = round((revision.toDouble() + cost) * 100) / 100

    fun snapshot(): PendingDutyPeriodPlan {
        return PendingDutyPeriodPlan(
            code = code,
            name = name,
            cost = cost,
            revision = revision,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending duty period plan in status created. */
fun newPendingDutyPeriodPlan(code: String, name: String): PendingDutyPeriodPlan {
    return PendingDutyPeriodPlan(
        code = code,
        name = name,
        cost = 0.0,
        revision = 0,
        timezone = "",
        status = PendingDutyPeriodPlanStatus.CREATED,
    )
}
