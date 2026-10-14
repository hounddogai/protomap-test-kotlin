package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization plan is in its life cycle. */
enum class UtilizationPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A utilization plan as the utilization team keeps it. */
data class UtilizationPlan(
    var code: String,
    var title: String,
    var weight: Double,
    var revision: Int,
    var budget: Double,
    var archived: Boolean,
    var status: UtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationPlanStatus): Boolean {
        when (status) {
            UtilizationPlanStatus.OPEN -> return next == UtilizationPlanStatus.REOPENED || next == UtilizationPlanStatus.ARCHIVED
            UtilizationPlanStatus.IN_REVIEW -> return next == UtilizationPlanStatus.REOPENED
            UtilizationPlanStatus.RESOLVED -> {
                when (next) {
                    UtilizationPlanStatus.REOPENED, UtilizationPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            UtilizationPlanStatus.REOPENED -> return next == UtilizationPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: UtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("utilization plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"utilization plan"} $code"

    fun effectiveExposure(): Double = round((budget + weight) * 100) / 100

    fun snapshot(): UtilizationPlan {
        return UtilizationPlan(
            code = code,
            title = title,
            weight = weight,
            revision = revision,
            budget = budget,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a utilization plan in status open. */
fun newUtilizationPlan(code: String, title: String): UtilizationPlan {
    return UtilizationPlan(
        code = code,
        title = title,
        weight = 0.0,
        revision = 0,
        budget = 0.0,
        archived = false,
        status = UtilizationPlanStatus.OPEN,
    )
}
