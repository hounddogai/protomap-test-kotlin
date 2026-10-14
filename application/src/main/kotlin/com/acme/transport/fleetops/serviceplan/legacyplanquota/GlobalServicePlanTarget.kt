package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize

/** Where a global service plan target is in its life cycle. */
enum class GlobalServicePlanTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global service plan target as the service plan team keeps it. */
data class GlobalServicePlanTarget(
    var code: String,
    var label: String,
    var title: String,
    var depot: String,
    var pinned: Boolean,
    var reviewer: String,
    var locale: String,
    var amount: Double,
    var status: GlobalServicePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServicePlanTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServicePlanTargetStatus): Boolean {
        when (status) {
            GlobalServicePlanTargetStatus.OPEN -> {
                when (next) {
                    GlobalServicePlanTargetStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalServicePlanTargetStatus.IN_REVIEW -> {
                when (next) {
                    GlobalServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalServicePlanTargetStatus.RESOLVED -> return next == GlobalServicePlanTargetStatus.REOPENED
            GlobalServicePlanTargetStatus.REOPENED -> {
                when (next) {
                    GlobalServicePlanTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalServicePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("global service plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global service plan target"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalServicePlanTarget {
        return GlobalServicePlanTarget(
            code = code,
            label = label,
            title = title,
            depot = depot,
            pinned = pinned,
            reviewer = reviewer,
            locale = locale,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global service plan target in status open. */
fun newGlobalServicePlanTarget(code: String, label: String): GlobalServicePlanTarget {
    return GlobalServicePlanTarget(
        code = code,
        label = label,
        title = "",
        depot = "",
        pinned = false,
        reviewer = "",
        locale = "",
        amount = 0.0,
        status = GlobalServicePlanTargetStatus.OPEN,
    )
}
