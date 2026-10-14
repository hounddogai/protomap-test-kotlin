package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending redelivery allocation records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a pending redelivery allocation is in its life cycle. */
enum class PendingRedeliveryAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending redelivery allocation, with what the planners need to act on it. */
data class PendingRedeliveryAllocation(
    var code: String,
    var label: String,
    var graceDays: Int,
    var description: String,
    var quantity: Int,
    var status: PendingRedeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRedeliveryAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRedeliveryAllocationStatus): Boolean {
        when (status) {
            PendingRedeliveryAllocationStatus.DRAFT -> return next == PendingRedeliveryAllocationStatus.SUSPENDED || next == PendingRedeliveryAllocationStatus.CLOSED
            PendingRedeliveryAllocationStatus.PENDING -> return next == PendingRedeliveryAllocationStatus.ACTIVE || next == PendingRedeliveryAllocationStatus.CLOSED
            PendingRedeliveryAllocationStatus.ACTIVE -> {
                when (next) {
                    PendingRedeliveryAllocationStatus.SUSPENDED, PendingRedeliveryAllocationStatus.CLOSED -> return true
                }
                return false
            }
            PendingRedeliveryAllocationStatus.SUSPENDED -> {
                when (next) {
                    PendingRedeliveryAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRedeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending redelivery allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((graceDays.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): PendingRedeliveryAllocation {
        return PendingRedeliveryAllocation(
            code = code,
            label = label,
            graceDays = graceDays,
            description = description,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending redelivery allocation in status draft. */
fun newPendingRedeliveryAllocation(code: String, label: String): PendingRedeliveryAllocation {
    return PendingRedeliveryAllocation(
        code = code,
        label = label,
        graceDays = 0,
        description = "",
        quantity = 0,
        status = PendingRedeliveryAllocationStatus.DRAFT,
    )
}
