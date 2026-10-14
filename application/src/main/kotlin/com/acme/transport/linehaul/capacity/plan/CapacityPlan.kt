package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize

// The capacity plan records of linehaul's capacity work, and the rules, stores, and calculations around them.

/** Where a capacity plan is in its life cycle. */
enum class CapacityPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capacity plan. Its code is unique within a tenant. */
data class CapacityPlan(
    var code: String,
    var label: String,
    var ceiling: Double,
    var pinned: Boolean,
    var ownerId: String,
    var status: CapacityPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityPlanStatus): Boolean {
        when (status) {
            CapacityPlanStatus.REQUESTED -> return next == CapacityPlanStatus.REVOKED
            CapacityPlanStatus.GRANTED -> {
                when (next) {
                    CapacityPlanStatus.REVOKED, CapacityPlanStatus.EXPIRED -> return true
                }
                return false
            }
            CapacityPlanStatus.REVOKED -> return next == CapacityPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CapacityPlanStatus) {
        if (!canMoveTo(next)) {
            error("capacity plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = ceiling

    fun snapshot(): CapacityPlan {
        return CapacityPlan(
            code = code,
            label = label,
            ceiling = ceiling,
            pinned = pinned,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity plan in status requested. */
fun newCapacityPlan(code: String, label: String): CapacityPlan {
    return CapacityPlan(
        code = code,
        label = label,
        ceiling = 0.0,
        pinned = false,
        ownerId = "",
        status = CapacityPlanStatus.REQUESTED,
    )
}
