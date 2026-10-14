package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize

// The capacity target records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity target is in its life cycle. */
enum class CapacityTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capacity target and its current status. */
data class CapacityTarget(
    var code: String,
    var label: String,
    var reviewer: String,
    var cost: Double,
    var segment: String,
    var status: CapacityTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityTargetStatus): Boolean {
        when (status) {
            CapacityTargetStatus.PROPOSED -> {
                when (next) {
                    CapacityTargetStatus.APPROVED, CapacityTargetStatus.REJECTED -> return true
                }
                return false
            }
            CapacityTargetStatus.APPROVED -> return next == CapacityTargetStatus.REJECTED
            CapacityTargetStatus.REJECTED -> {
                when (next) {
                    CapacityTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityTargetStatus) {
        if (!canMoveTo(next)) {
            error("capacity target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capacity target"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): CapacityTarget {
        return CapacityTarget(
            code = code,
            label = label,
            reviewer = reviewer,
            cost = cost,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capacity target in status proposed. */
fun newCapacityTarget(code: String, label: String): CapacityTarget {
    return CapacityTarget(
        code = code,
        label = label,
        reviewer = "",
        cost = 0.0,
        segment = "",
        status = CapacityTargetStatus.PROPOSED,
    )
}
