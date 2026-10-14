package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending broker allocation is in its life cycle. */
enum class PendingBrokerAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending broker allocation as the broker team keeps it. */
data class PendingBrokerAllocation(
    var code: String,
    var name: String,
    var discount: Double,
    var reviewer: String,
    var unit: String,
    var stopCount: Int,
    var active: Boolean,
    var carrier: String,
    var status: PendingBrokerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBrokerAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBrokerAllocationStatus): Boolean {
        when (status) {
            PendingBrokerAllocationStatus.DRAFT -> return next == PendingBrokerAllocationStatus.ACTIVE
            PendingBrokerAllocationStatus.PENDING -> {
                when (next) {
                    PendingBrokerAllocationStatus.ACTIVE -> return true
                }
                return false
            }
            PendingBrokerAllocationStatus.ACTIVE -> return next == PendingBrokerAllocationStatus.SUSPENDED || next == PendingBrokerAllocationStatus.CLOSED
            PendingBrokerAllocationStatus.SUSPENDED -> {
                when (next) {
                    PendingBrokerAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingBrokerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending broker allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round((discount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): PendingBrokerAllocation {
        return PendingBrokerAllocation(
            code = code,
            name = name,
            discount = discount,
            reviewer = reviewer,
            unit = unit,
            stopCount = stopCount,
            active = active,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending broker allocation in status draft. */
fun newPendingBrokerAllocation(code: String, name: String): PendingBrokerAllocation {
    return PendingBrokerAllocation(
        code = code,
        name = name,
        discount = 0.0,
        reviewer = "",
        unit = "",
        stopCount = 0,
        active = false,
        carrier = "",
        status = PendingBrokerAllocationStatus.DRAFT,
    )
}
