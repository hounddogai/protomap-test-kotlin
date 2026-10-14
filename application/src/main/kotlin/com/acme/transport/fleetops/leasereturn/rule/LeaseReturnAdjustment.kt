package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return adjustment is in its life cycle. */
enum class LeaseReturnAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lease return adjustment. Its code is unique within a tenant. */
data class LeaseReturnAdjustment(
    var code: String,
    var name: String,
    var loadKg: Double,
    var trailerId: String,
    var discount: Double,
    var status: LeaseReturnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnAdjustmentStatus): Boolean {
        when (status) {
            LeaseReturnAdjustmentStatus.PROPOSED -> {
                when (next) {
                    LeaseReturnAdjustmentStatus.APPROVED, LeaseReturnAdjustmentStatus.REJECTED -> return true
                }
                return false
            }
            LeaseReturnAdjustmentStatus.APPROVED -> return next == LeaseReturnAdjustmentStatus.REJECTED || next == LeaseReturnAdjustmentStatus.WITHDRAWN
            LeaseReturnAdjustmentStatus.REJECTED -> {
                when (next) {
                    LeaseReturnAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("lease return adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((discount - loadKg) * 100) / 100

    fun snapshot(): LeaseReturnAdjustment {
        return LeaseReturnAdjustment(
            code = code,
            name = name,
            loadKg = loadKg,
            trailerId = trailerId,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return adjustment in status proposed. */
fun newLeaseReturnAdjustment(code: String, name: String): LeaseReturnAdjustment {
    return LeaseReturnAdjustment(
        code = code,
        name = name,
        loadKg = 0.0,
        trailerId = "",
        discount = 0.0,
        status = LeaseReturnAdjustmentStatus.PROPOSED,
    )
}
