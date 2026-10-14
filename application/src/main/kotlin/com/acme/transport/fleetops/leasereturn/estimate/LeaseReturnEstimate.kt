package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return estimate records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return estimate is in its life cycle. */
enum class LeaseReturnEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return estimate, with what the planners need to act on it. */
data class LeaseReturnEstimate(
    var code: String,
    var title: String,
    var floorValue: Double,
    var category: String,
    var quantity: Int,
    var margin: Double,
    var source: String,
    var visible: Boolean,
    var status: LeaseReturnEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnEstimateStatus): Boolean {
        when (status) {
            LeaseReturnEstimateStatus.PLANNED -> return next == LeaseReturnEstimateStatus.IN_PROGRESS || next == LeaseReturnEstimateStatus.CANCELLED
            LeaseReturnEstimateStatus.SCHEDULED -> return next == LeaseReturnEstimateStatus.COMPLETED || next == LeaseReturnEstimateStatus.CANCELLED
            LeaseReturnEstimateStatus.IN_PROGRESS -> return next == LeaseReturnEstimateStatus.COMPLETED || next == LeaseReturnEstimateStatus.CANCELLED
            LeaseReturnEstimateStatus.COMPLETED -> return next == LeaseReturnEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lease return estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round(quantity.toDouble() * floorValue * 100) / 100

    fun snapshot(): LeaseReturnEstimate {
        return LeaseReturnEstimate(
            code = code,
            title = title,
            floorValue = floorValue,
            category = category,
            quantity = quantity,
            margin = margin,
            source = source,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lease return estimate in status planned. */
fun newLeaseReturnEstimate(code: String, title: String): LeaseReturnEstimate {
    return LeaseReturnEstimate(
        code = code,
        title = title,
        floorValue = 0.0,
        category = "",
        quantity = 0,
        margin = 0.0,
        source = "",
        visible = false,
        status = LeaseReturnEstimateStatus.PLANNED,
    )
}
