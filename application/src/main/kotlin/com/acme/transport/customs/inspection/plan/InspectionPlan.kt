package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection plan records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection plan is in its life cycle. */
enum class InspectionPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A inspection plan as the inspection team keeps it. */
data class InspectionPlan(
    var code: String,
    var label: String,
    var priority: Int,
    var description: String,
    var batchSize: Int,
    var stopCount: Int,
    var status: InspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionPlanStatus): Boolean {
        when (status) {
            InspectionPlanStatus.PROPOSED -> {
                when (next) {
                    InspectionPlanStatus.REJECTED -> return true
                }
                return false
            }
            InspectionPlanStatus.APPROVED -> {
                when (next) {
                    InspectionPlanStatus.REJECTED, InspectionPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionPlanStatus.REJECTED -> {
                when (next) {
                    InspectionPlanStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection plan"} $code"

    fun netExposure(): Double = round((stopCount.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): InspectionPlan {
        return InspectionPlan(
            code = code,
            label = label,
            priority = priority,
            description = description,
            batchSize = batchSize,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection plan in status proposed. */
fun newInspectionPlan(code: String, label: String): InspectionPlan {
    return InspectionPlan(
        code = code,
        label = label,
        priority = 0,
        description = "",
        batchSize = 0,
        stopCount = 0,
        status = InspectionPlanStatus.PROPOSED,
    )
}
