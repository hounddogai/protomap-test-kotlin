package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return plan is in its life cycle. */
enum class LeaseReturnPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return plan, with what the planners need to act on it. */
data class LeaseReturnPlan(
    var code: String,
    var title: String,
    var dock: String,
    var pinned: Boolean,
    var version: Int,
    var unit: String,
    var category: String,
    var minimum: Double,
    var approved: Boolean,
    var status: LeaseReturnPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnPlanStatus): Boolean {
        when (status) {
            LeaseReturnPlanStatus.PLANNED -> return next == LeaseReturnPlanStatus.SCHEDULED
            LeaseReturnPlanStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnPlanStatus.IN_PROGRESS, LeaseReturnPlanStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnPlanStatus.IN_PROGRESS -> {
                when (next) {
                    LeaseReturnPlanStatus.COMPLETED, LeaseReturnPlanStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnPlanStatus.COMPLETED -> {
                when (next) {
                    LeaseReturnPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnPlanStatus) {
        if (!canMoveTo(next)) {
            error("lease return plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round((version.toDouble() + minimum) * 100) / 100

    fun snapshot(): LeaseReturnPlan {
        return LeaseReturnPlan(
            code = code,
            title = title,
            dock = dock,
            pinned = pinned,
            version = version,
            unit = unit,
            category = category,
            minimum = minimum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lease return plan in status planned. */
fun newLeaseReturnPlan(code: String, title: String): LeaseReturnPlan {
    return LeaseReturnPlan(
        code = code,
        title = title,
        dock = "",
        pinned = false,
        version = 0,
        unit = "",
        category = "",
        minimum = 0.0,
        approved = false,
        status = LeaseReturnPlanStatus.PLANNED,
    )
}
