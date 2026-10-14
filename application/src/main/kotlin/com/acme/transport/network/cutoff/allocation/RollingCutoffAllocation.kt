package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling cutoff allocation records of network's cutoff work, and the rules, stores, and calculations
// around them.

/** Where a rolling cutoff allocation is in its life cycle. */
enum class RollingCutoffAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling cutoff allocation and its current status. */
data class RollingCutoffAllocation(
    var code: String,
    var name: String,
    var stopCount: Int,
    var priority: Int,
    var odometer: Int,
    var notes: String,
    var discount: Double,
    var status: RollingCutoffAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffAllocationStatus): Boolean {
        when (status) {
            RollingCutoffAllocationStatus.IDLE -> return next == RollingCutoffAllocationStatus.READY || next == RollingCutoffAllocationStatus.DRAINING
            RollingCutoffAllocationStatus.WARMING -> return next == RollingCutoffAllocationStatus.DRAINING || next == RollingCutoffAllocationStatus.STOPPED
            RollingCutoffAllocationStatus.READY -> return next == RollingCutoffAllocationStatus.STOPPED
            RollingCutoffAllocationStatus.DRAINING -> {
                when (next) {
                    RollingCutoffAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCutoffAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling cutoff allocation"} $code"

    fun projectedExposure(): Double = round(priority.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): RollingCutoffAllocation {
        return RollingCutoffAllocation(
            code = code,
            name = name,
            stopCount = stopCount,
            priority = priority,
            odometer = odometer,
            notes = notes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling cutoff allocation in status idle. */
fun newRollingCutoffAllocation(code: String, name: String): RollingCutoffAllocation {
    return RollingCutoffAllocation(
        code = code,
        name = name,
        stopCount = 0,
        priority = 0,
        odometer = 0,
        notes = "",
        discount = 0.0,
        status = RollingCutoffAllocationStatus.IDLE,
    )
}
