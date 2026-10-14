package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled inspection plan is in its life cycle. */
enum class ScheduledInspectionPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled inspection plan and its current status. */
data class ScheduledInspectionPlan(
    var code: String,
    var name: String,
    var revision: Int,
    var updatedAt: Int,
    var pallets: Int,
    var description: String,
    var retries: Int,
    var discount: Double,
    var status: ScheduledInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledInspectionPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledInspectionPlanStatus): Boolean {
        when (status) {
            ScheduledInspectionPlanStatus.OPEN -> return next == ScheduledInspectionPlanStatus.IN_REVIEW || next == ScheduledInspectionPlanStatus.RESOLVED
            ScheduledInspectionPlanStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledInspectionPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledInspectionPlanStatus.RESOLVED -> return next == ScheduledInspectionPlanStatus.REOPENED || next == ScheduledInspectionPlanStatus.ARCHIVED
            ScheduledInspectionPlanStatus.REOPENED -> return next == ScheduledInspectionPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("scheduled inspection plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"scheduled inspection plan"} $code"

    fun adjustedLoad(): Double = round((pallets.toDouble() - discount) * 100) / 100

    fun snapshot(): ScheduledInspectionPlan {
        return ScheduledInspectionPlan(
            code = code,
            name = name,
            revision = revision,
            updatedAt = updatedAt,
            pallets = pallets,
            description = description,
            retries = retries,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled inspection plan in status open. */
fun newScheduledInspectionPlan(code: String, name: String): ScheduledInspectionPlan {
    return ScheduledInspectionPlan(
        code = code,
        name = name,
        revision = 0,
        updatedAt = 0,
        pallets = 0,
        description = "",
        retries = 0,
        discount = 0.0,
        status = ScheduledInspectionPlanStatus.OPEN,
    )
}
