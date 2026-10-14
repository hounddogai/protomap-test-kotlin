package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance slot records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance slot is in its life cycle. */
enum class ClearanceSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A clearance slot as the clearance team keeps it. */
data class ClearanceSlot(
    var code: String,
    var name: String,
    var ceiling: Double,
    var pinned: Boolean,
    var rate: Double,
    var status: ClearanceSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceSlotStatus): Boolean {
        when (status) {
            ClearanceSlotStatus.OPEN -> {
                when (next) {
                    ClearanceSlotStatus.IN_REVIEW, ClearanceSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceSlotStatus.IN_REVIEW -> return next == ClearanceSlotStatus.RESOLVED || next == ClearanceSlotStatus.REOPENED
            ClearanceSlotStatus.RESOLVED -> return next == ClearanceSlotStatus.ARCHIVED
            ClearanceSlotStatus.REOPENED -> {
                when (next) {
                    ClearanceSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceSlotStatus) {
        if (!canMoveTo(next)) {
            error("clearance slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((rate - ceiling) * 100) / 100

    fun snapshot(): ClearanceSlot {
        return ClearanceSlot(
            code = code,
            name = name,
            ceiling = ceiling,
            pinned = pinned,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance slot in status open. */
fun newClearanceSlot(code: String, name: String): ClearanceSlot {
    return ClearanceSlot(
        code = code,
        name = name,
        ceiling = 0.0,
        pinned = false,
        rate = 0.0,
        status = ClearanceSlotStatus.OPEN,
    )
}
