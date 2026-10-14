package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.normalize

// The adaptive utilization plan records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a adaptive utilization plan is in its life cycle. */
enum class AdaptiveUtilizationPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive utilization plan. Its code is unique within a tenant. */
data class AdaptiveUtilizationPlan(
    var code: String,
    var title: String,
    var budget: Double,
    var summary: String,
    var routeCode: String,
    var status: AdaptiveUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveUtilizationPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveUtilizationPlanStatus): Boolean {
        when (status) {
            AdaptiveUtilizationPlanStatus.IDLE -> return next == AdaptiveUtilizationPlanStatus.STOPPED
            AdaptiveUtilizationPlanStatus.WARMING -> {
                when (next) {
                    AdaptiveUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveUtilizationPlanStatus.READY -> {
                when (next) {
                    AdaptiveUtilizationPlanStatus.DRAINING, AdaptiveUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveUtilizationPlanStatus.DRAINING -> {
                when (next) {
                    AdaptiveUtilizationPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("adaptive utilization plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive utilization plan"} $code"

    fun measuredValue(): Double = budget

    fun snapshot(): AdaptiveUtilizationPlan {
        return AdaptiveUtilizationPlan(
            code = code,
            title = title,
            budget = budget,
            summary = summary,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive utilization plan in status idle. */
fun newAdaptiveUtilizationPlan(code: String, title: String): AdaptiveUtilizationPlan {
    return AdaptiveUtilizationPlan(
        code = code,
        title = title,
        budget = 0.0,
        summary = "",
        routeCode = "",
        status = AdaptiveUtilizationPlanStatus.IDLE,
    )
}
