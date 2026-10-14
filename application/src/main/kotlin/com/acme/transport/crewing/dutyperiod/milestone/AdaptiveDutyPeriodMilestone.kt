package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive duty period milestone records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a adaptive duty period milestone is in its life cycle. */
enum class AdaptiveDutyPeriodMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive duty period milestone. Its code is unique within a tenant. */
data class AdaptiveDutyPeriodMilestone(
    var code: String,
    var title: String,
    var priority: Int,
    var label: String,
    var description: String,
    var loadKg: Double,
    var endsAt: Int,
    var cost: Double,
    var routeCode: String,
    var distanceKm: Double,
    var dock: String,
    var status: AdaptiveDutyPeriodMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDutyPeriodMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDutyPeriodMilestoneStatus): Boolean {
        when (status) {
            AdaptiveDutyPeriodMilestoneStatus.REQUESTED -> return next == AdaptiveDutyPeriodMilestoneStatus.GRANTED || next == AdaptiveDutyPeriodMilestoneStatus.EXPIRED
            AdaptiveDutyPeriodMilestoneStatus.GRANTED -> {
                when (next) {
                    AdaptiveDutyPeriodMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveDutyPeriodMilestoneStatus.REVOKED -> return next == AdaptiveDutyPeriodMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveDutyPeriodMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive duty period milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedValue(): Double = round((cost - endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDutyPeriodMilestone {
        return AdaptiveDutyPeriodMilestone(
            code = code,
            title = title,
            priority = priority,
            label = label,
            description = description,
            loadKg = loadKg,
            endsAt = endsAt,
            cost = cost,
            routeCode = routeCode,
            distanceKm = distanceKm,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive duty period milestone in status requested. */
fun newAdaptiveDutyPeriodMilestone(code: String, title: String): AdaptiveDutyPeriodMilestone {
    return AdaptiveDutyPeriodMilestone(
        code = code,
        title = title,
        priority = 0,
        label = "",
        description = "",
        loadKg = 0.0,
        endsAt = 0,
        cost = 0.0,
        routeCode = "",
        distanceKm = 0.0,
        dock = "",
        status = AdaptiveDutyPeriodMilestoneStatus.REQUESTED,
    )
}
