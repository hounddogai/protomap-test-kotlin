package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial plan records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial plan is in its life cycle. */
enum class AccessorialPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One accessorial plan, with what the planners need to act on it. */
data class AccessorialPlan(
    var code: String,
    var label: String,
    var weight: Double,
    var quantity: Int,
    var ownerId: String,
    var dock: String,
    var depot: String,
    var status: AccessorialPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialPlanStatus): Boolean {
        when (status) {
            AccessorialPlanStatus.OPEN -> return next == AccessorialPlanStatus.RESOLVED || next == AccessorialPlanStatus.ARCHIVED
            AccessorialPlanStatus.IN_REVIEW -> return next == AccessorialPlanStatus.REOPENED
            AccessorialPlanStatus.RESOLVED -> {
                when (next) {
                    AccessorialPlanStatus.REOPENED, AccessorialPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialPlanStatus.REOPENED -> return next == AccessorialPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AccessorialPlanStatus) {
        if (!canMoveTo(next)) {
            error("accessorial plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((weight + quantity.toDouble()) * 100) / 100

    fun snapshot(): AccessorialPlan {
        return AccessorialPlan(
            code = code,
            label = label,
            weight = weight,
            quantity = quantity,
            ownerId = ownerId,
            dock = dock,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a accessorial plan in status open. */
fun newAccessorialPlan(code: String, label: String): AccessorialPlan {
    return AccessorialPlan(
        code = code,
        label = label,
        weight = 0.0,
        quantity = 0,
        ownerId = "",
        dock = "",
        depot = "",
        status = AccessorialPlanStatus.OPEN,
    )
}
