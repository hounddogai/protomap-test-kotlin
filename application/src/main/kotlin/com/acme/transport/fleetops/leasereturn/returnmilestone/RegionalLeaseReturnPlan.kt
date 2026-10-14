package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.normalize

/** Where a regional lease return plan is in its life cycle. */
enum class RegionalLeaseReturnPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional lease return plan and its current status. */
data class RegionalLeaseReturnPlan(
    var code: String,
    var title: String,
    var category: String,
    var attempts: Int,
    var currency: String,
    var status: RegionalLeaseReturnPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnPlanStatus): Boolean {
        when (status) {
            RegionalLeaseReturnPlanStatus.CREATED -> {
                when (next) {
                    RegionalLeaseReturnPlanStatus.PUBLISHED, RegionalLeaseReturnPlanStatus.RETIRED -> return true
                }
                return false
            }
            RegionalLeaseReturnPlanStatus.VALIDATED -> {
                when (next) {
                    RegionalLeaseReturnPlanStatus.PUBLISHED, RegionalLeaseReturnPlanStatus.RETIRED -> return true
                }
                return false
            }
            RegionalLeaseReturnPlanStatus.PUBLISHED -> return next == RegionalLeaseReturnPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional lease return plan"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): RegionalLeaseReturnPlan {
        return RegionalLeaseReturnPlan(
            code = code,
            title = title,
            category = category,
            attempts = attempts,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional lease return plan in status created. */
fun newRegionalLeaseReturnPlan(code: String, title: String): RegionalLeaseReturnPlan {
    return RegionalLeaseReturnPlan(
        code = code,
        title = title,
        category = "",
        attempts = 0,
        currency = "",
        status = RegionalLeaseReturnPlanStatus.CREATED,
    )
}
