package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection plan is in its life cycle. */
enum class PrimaryInspectionPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary inspection plan, with what the planners need to act on it. */
data class PrimaryInspectionPlan(
    var code: String,
    var label: String,
    var retries: Int,
    var archived: Boolean,
    var priority: Int,
    var ownerId: String,
    var approved: Boolean,
    var status: PrimaryInspectionPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionPlanStatus): Boolean {
        when (status) {
            PrimaryInspectionPlanStatus.OPEN -> return next == PrimaryInspectionPlanStatus.ARCHIVED
            PrimaryInspectionPlanStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryInspectionPlanStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionPlanStatus.RESOLVED -> {
                when (next) {
                    PrimaryInspectionPlanStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryInspectionPlanStatus.REOPENED -> return next == PrimaryInspectionPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionPlanStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((priority.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryInspectionPlan {
        return PrimaryInspectionPlan(
            code = code,
            label = label,
            retries = retries,
            archived = archived,
            priority = priority,
            ownerId = ownerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary inspection plan in status open. */
fun newPrimaryInspectionPlan(code: String, label: String): PrimaryInspectionPlan {
    return PrimaryInspectionPlan(
        code = code,
        label = label,
        retries = 0,
        archived = false,
        priority = 0,
        ownerId = "",
        approved = false,
        status = PrimaryInspectionPlanStatus.OPEN,
    )
}
