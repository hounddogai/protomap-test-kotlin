package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection plan records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection plan is in its life cycle. */
enum class InspectionPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One inspection plan, with what the planners need to act on it. */
data class InspectionPlan(
    var code: String,
    var name: String,
    var maximum: Double,
    var startsAt: Int,
    var etaMinutes: Int,
    var weight: Double,
    var shiftCode: String,
    var status: InspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionPlanStatus): Boolean {
        when (status) {
            InspectionPlanStatus.IDLE -> return next == InspectionPlanStatus.WARMING
            InspectionPlanStatus.WARMING -> return next == InspectionPlanStatus.DRAINING || next == InspectionPlanStatus.STOPPED
            InspectionPlanStatus.READY -> return next == InspectionPlanStatus.DRAINING || next == InspectionPlanStatus.STOPPED
            InspectionPlanStatus.DRAINING -> return next == InspectionPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: InspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("inspection plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((startsAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): InspectionPlan {
        return InspectionPlan(
            code = code,
            name = name,
            maximum = maximum,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            weight = weight,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection plan in status idle. */
fun newInspectionPlan(code: String, name: String): InspectionPlan {
    return InspectionPlan(
        code = code,
        name = name,
        maximum = 0.0,
        startsAt = 0,
        etaMinutes = 0,
        weight = 0.0,
        shiftCode = "",
        status = InspectionPlanStatus.IDLE,
    )
}
