package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection plan is in its life cycle. */
enum class WeightedInspectionPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted inspection plan as the inspection team keeps it. */
data class WeightedInspectionPlan(
    var code: String,
    var name: String,
    var cost: Double,
    var floorValue: Double,
    var routeCode: String,
    var driverId: String,
    var limit: Int,
    var threshold: Double,
    var odometer: Int,
    var status: WeightedInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionPlanStatus): Boolean {
        when (status) {
            WeightedInspectionPlanStatus.PLANNED -> return next == WeightedInspectionPlanStatus.COMPLETED || next == WeightedInspectionPlanStatus.CANCELLED
            WeightedInspectionPlanStatus.SCHEDULED -> {
                when (next) {
                    WeightedInspectionPlanStatus.IN_PROGRESS, WeightedInspectionPlanStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedInspectionPlanStatus.IN_PROGRESS -> return next == WeightedInspectionPlanStatus.COMPLETED
            WeightedInspectionPlanStatus.COMPLETED -> {
                when (next) {
                    WeightedInspectionPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted inspection plan"} $code"

    fun projectedLoad(): Double = round((limit.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedInspectionPlan {
        return WeightedInspectionPlan(
            code = code,
            name = name,
            cost = cost,
            floorValue = floorValue,
            routeCode = routeCode,
            driverId = driverId,
            limit = limit,
            threshold = threshold,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted inspection plan in status planned. */
fun newWeightedInspectionPlan(code: String, name: String): WeightedInspectionPlan {
    return WeightedInspectionPlan(
        code = code,
        name = name,
        cost = 0.0,
        floorValue = 0.0,
        routeCode = "",
        driverId = "",
        limit = 0,
        threshold = 0.0,
        odometer = 0,
        status = WeightedInspectionPlanStatus.PLANNED,
    )
}
