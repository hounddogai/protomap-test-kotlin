package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.normalize

// The global proof of delivery allocation records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a global proof of delivery allocation is in its life cycle. */
enum class GlobalProofOfDeliveryAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global proof of delivery allocation as the proof of delivery team keeps it. */
data class GlobalProofOfDeliveryAllocation(
    var code: String,
    var name: String,
    var minimum: Double,
    var channel: String,
    var timezone: String,
    var status: GlobalProofOfDeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalProofOfDeliveryAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalProofOfDeliveryAllocationStatus): Boolean {
        when (status) {
            GlobalProofOfDeliveryAllocationStatus.PLANNED -> return next == GlobalProofOfDeliveryAllocationStatus.COMPLETED
            GlobalProofOfDeliveryAllocationStatus.SCHEDULED -> return next == GlobalProofOfDeliveryAllocationStatus.IN_PROGRESS
            GlobalProofOfDeliveryAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalProofOfDeliveryAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalProofOfDeliveryAllocationStatus.COMPLETED -> {
                when (next) {
                    GlobalProofOfDeliveryAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalProofOfDeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global proof of delivery allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global proof of delivery allocation"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): GlobalProofOfDeliveryAllocation {
        return GlobalProofOfDeliveryAllocation(
            code = code,
            name = name,
            minimum = minimum,
            channel = channel,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global proof of delivery allocation in status planned. */
fun newGlobalProofOfDeliveryAllocation(code: String, name: String): GlobalProofOfDeliveryAllocation {
    return GlobalProofOfDeliveryAllocation(
        code = code,
        name = name,
        minimum = 0.0,
        channel = "",
        timezone = "",
        status = GlobalProofOfDeliveryAllocationStatus.PLANNED,
    )
}
