package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending idling allocation is in its life cycle. */
enum class PendingIdlingAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending idling allocation as the idling team keeps it. */
data class PendingIdlingAllocation(
    var code: String,
    var name: String,
    var version: Int,
    var source: String,
    var priority: Int,
    var unit: String,
    var status: PendingIdlingAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingAllocationStatus): Boolean {
        when (status) {
            PendingIdlingAllocationStatus.PROPOSED -> {
                when (next) {
                    PendingIdlingAllocationStatus.REJECTED -> return true
                }
                return false
            }
            PendingIdlingAllocationStatus.APPROVED -> return next == PendingIdlingAllocationStatus.REJECTED || next == PendingIdlingAllocationStatus.WITHDRAWN
            PendingIdlingAllocationStatus.REJECTED -> return next == PendingIdlingAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingIdlingAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending idling allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"pending idling allocation"} $code"

    fun weightedExposure(): Double = round((version.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): PendingIdlingAllocation {
        return PendingIdlingAllocation(
            code = code,
            name = name,
            version = version,
            source = source,
            priority = priority,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending idling allocation in status proposed. */
fun newPendingIdlingAllocation(code: String, name: String): PendingIdlingAllocation {
    return PendingIdlingAllocation(
        code = code,
        name = name,
        version = 0,
        source = "",
        priority = 0,
        unit = "",
        status = PendingIdlingAllocationStatus.PROPOSED,
    )
}
