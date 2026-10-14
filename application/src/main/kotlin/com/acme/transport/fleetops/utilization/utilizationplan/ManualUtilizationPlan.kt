package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual utilization plan records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a manual utilization plan is in its life cycle. */
enum class ManualUtilizationPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual utilization plan as the utilization team keeps it. */
data class ManualUtilizationPlan(
    var code: String,
    var name: String,
    var floorValue: Double,
    var timezone: String,
    var visible: Boolean,
    var cost: Double,
    var trailerId: String,
    var status: ManualUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationPlanStatus): Boolean {
        when (status) {
            ManualUtilizationPlanStatus.PROPOSED -> return next == ManualUtilizationPlanStatus.REJECTED || next == ManualUtilizationPlanStatus.WITHDRAWN
            ManualUtilizationPlanStatus.APPROVED -> {
                when (next) {
                    ManualUtilizationPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualUtilizationPlanStatus.REJECTED -> return next == ManualUtilizationPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round(cost * floorValue * 100) / 100

    fun snapshot(): ManualUtilizationPlan {
        return ManualUtilizationPlan(
            code = code,
            name = name,
            floorValue = floorValue,
            timezone = timezone,
            visible = visible,
            cost = cost,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual utilization plan in status proposed. */
fun newManualUtilizationPlan(code: String, name: String): ManualUtilizationPlan {
    return ManualUtilizationPlan(
        code = code,
        name = name,
        floorValue = 0.0,
        timezone = "",
        visible = false,
        cost = 0.0,
        trailerId = "",
        status = ManualUtilizationPlanStatus.PROPOSED,
    )
}
