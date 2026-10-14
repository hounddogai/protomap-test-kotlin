package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize

// The capacity allocation records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity allocation is in its life cycle. */
enum class CapacityAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capacity allocation as the capacity team keeps it. */
data class CapacityAllocation(
    var code: String,
    var label: String,
    var threshold: Double,
    var contactEmail: String,
    var channel: String,
    var status: CapacityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityAllocationStatus): Boolean {
        when (status) {
            CapacityAllocationStatus.IDLE -> return next == CapacityAllocationStatus.READY || next == CapacityAllocationStatus.DRAINING
            CapacityAllocationStatus.WARMING -> return next == CapacityAllocationStatus.DRAINING
            CapacityAllocationStatus.READY -> return next == CapacityAllocationStatus.DRAINING || next == CapacityAllocationStatus.STOPPED
            CapacityAllocationStatus.DRAINING -> {
                when (next) {
                    CapacityAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("capacity allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = threshold

    fun snapshot(): CapacityAllocation {
        return CapacityAllocation(
            code = code,
            label = label,
            threshold = threshold,
            contactEmail = contactEmail,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capacity allocation in status idle. */
fun newCapacityAllocation(code: String, label: String): CapacityAllocation {
    return CapacityAllocation(
        code = code,
        label = label,
        threshold = 0.0,
        contactEmail = "",
        channel = "",
        status = CapacityAllocationStatus.IDLE,
    )
}
