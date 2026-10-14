package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient utilization plan is in its life cycle. */
enum class LenientUtilizationPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lenient utilization plan, with what the planners need to act on it. */
data class LenientUtilizationPlan(
    var code: String,
    var title: String,
    var currency: String,
    var depot: String,
    var discount: Double,
    var capacity: Int,
    var status: LenientUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientUtilizationPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientUtilizationPlanStatus): Boolean {
        when (status) {
            LenientUtilizationPlanStatus.PROPOSED -> {
                when (next) {
                    LenientUtilizationPlanStatus.REJECTED -> return true
                }
                return false
            }
            LenientUtilizationPlanStatus.APPROVED -> return next == LenientUtilizationPlanStatus.WITHDRAWN
            LenientUtilizationPlanStatus.REJECTED -> return next == LenientUtilizationPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("lenient utilization plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((discount - capacity.toDouble()) * 100) / 100

    fun snapshot(): LenientUtilizationPlan {
        return LenientUtilizationPlan(
            code = code,
            title = title,
            currency = currency,
            depot = depot,
            discount = discount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient utilization plan in status proposed. */
fun newLenientUtilizationPlan(code: String, title: String): LenientUtilizationPlan {
    return LenientUtilizationPlan(
        code = code,
        title = title,
        currency = "",
        depot = "",
        discount = 0.0,
        capacity = 0,
        status = LenientUtilizationPlanStatus.PROPOSED,
    )
}
