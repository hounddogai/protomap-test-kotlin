package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize

/** Where a rolling route offset plan is in its life cycle. */
enum class RollingRouteOffsetPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling route offset plan, with what the planners need to act on it. */
data class RollingRouteOffsetPlan(
    var code: String,
    var label: String,
    var routeCode: String,
    var dock: String,
    var endsAt: Int,
    var status: RollingRouteOffsetPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRouteOffsetPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRouteOffsetPlanStatus): Boolean {
        when (status) {
            RollingRouteOffsetPlanStatus.CREATED -> return next == RollingRouteOffsetPlanStatus.VALIDATED
            RollingRouteOffsetPlanStatus.VALIDATED -> return next == RollingRouteOffsetPlanStatus.PUBLISHED
            RollingRouteOffsetPlanStatus.PUBLISHED -> {
                when (next) {
                    RollingRouteOffsetPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRouteOffsetPlanStatus) {
        if (!canMoveTo(next)) {
            error("rolling route offset plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): RollingRouteOffsetPlan {
        return RollingRouteOffsetPlan(
            code = code,
            label = label,
            routeCode = routeCode,
            dock = dock,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling route offset plan in status created. */
fun newRollingRouteOffsetPlan(code: String, label: String): RollingRouteOffsetPlan {
    return RollingRouteOffsetPlan(
        code = code,
        label = label,
        routeCode = "",
        dock = "",
        endsAt = 0,
        status = RollingRouteOffsetPlanStatus.CREATED,
    )
}
