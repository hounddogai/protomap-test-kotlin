package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.text.normalize

/** Where a near miss allocation is in its life cycle. */
enum class NearMissAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A near miss allocation as the near miss team keeps it. */
data class NearMissAllocation(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var shiftCode: String,
    var maximum: Double,
    var status: NearMissAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissAllocationStatus): Boolean {
        when (status) {
            NearMissAllocationStatus.DRAFT -> return next == NearMissAllocationStatus.SUSPENDED
            NearMissAllocationStatus.PENDING -> {
                when (next) {
                    NearMissAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            NearMissAllocationStatus.ACTIVE -> {
                when (next) {
                    NearMissAllocationStatus.SUSPENDED, NearMissAllocationStatus.CLOSED -> return true
                }
                return false
            }
            NearMissAllocationStatus.SUSPENDED -> {
                when (next) {
                    NearMissAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissAllocationStatus) {
        if (!canMoveTo(next)) {
            error("near miss allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = maximum

    fun snapshot(): NearMissAllocation {
        return NearMissAllocation(
            code = code,
            name = name,
            pinned = pinned,
            shiftCode = shiftCode,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss allocation in status draft. */
fun newNearMissAllocation(code: String, name: String): NearMissAllocation {
    return NearMissAllocation(
        code = code,
        name = name,
        pinned = false,
        shiftCode = "",
        maximum = 0.0,
        status = NearMissAllocationStatus.DRAFT,
    )
}
