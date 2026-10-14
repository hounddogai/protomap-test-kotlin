package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize

/** Where a global lease return adjustment is in its life cycle. */
enum class GlobalLeaseReturnAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global lease return adjustment. Its code is unique within a tenant. */
data class GlobalLeaseReturnAdjustment(
    var code: String,
    var name: String,
    var approved: Boolean,
    var description: String,
    var active: Boolean,
    var label: String,
    var timezone: String,
    var amount: Double,
    var status: GlobalLeaseReturnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnAdjustmentStatus): Boolean {
        when (status) {
            GlobalLeaseReturnAdjustmentStatus.PROPOSED -> return next == GlobalLeaseReturnAdjustmentStatus.WITHDRAWN
            GlobalLeaseReturnAdjustmentStatus.APPROVED -> return next == GlobalLeaseReturnAdjustmentStatus.WITHDRAWN
            GlobalLeaseReturnAdjustmentStatus.REJECTED -> return next == GlobalLeaseReturnAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global lease return adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global lease return adjustment"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalLeaseReturnAdjustment {
        return GlobalLeaseReturnAdjustment(
            code = code,
            name = name,
            approved = approved,
            description = description,
            active = active,
            label = label,
            timezone = timezone,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global lease return adjustment in status proposed. */
fun newGlobalLeaseReturnAdjustment(code: String, name: String): GlobalLeaseReturnAdjustment {
    return GlobalLeaseReturnAdjustment(
        code = code,
        name = name,
        approved = false,
        description = "",
        active = false,
        label = "",
        timezone = "",
        amount = 0.0,
        status = GlobalLeaseReturnAdjustmentStatus.PROPOSED,
    )
}
