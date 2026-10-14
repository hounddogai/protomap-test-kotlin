package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.text.normalize

// The pending drop slot records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a pending drop slot is in its life cycle. */
enum class PendingDropSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending drop slot as the drop team keeps it. */
data class PendingDropSlot(
    var code: String,
    var name: String,
    var ownerId: String,
    var shiftCode: String,
    var odometer: Int,
    var status: PendingDropSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDropSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDropSlotStatus): Boolean {
        when (status) {
            PendingDropSlotStatus.REQUESTED -> return next == PendingDropSlotStatus.EXPIRED
            PendingDropSlotStatus.GRANTED -> {
                when (next) {
                    PendingDropSlotStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDropSlotStatus.REVOKED -> return next == PendingDropSlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDropSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending drop slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending drop slot"} $code"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): PendingDropSlot {
        return PendingDropSlot(
            code = code,
            name = name,
            ownerId = ownerId,
            shiftCode = shiftCode,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending drop slot in status requested. */
fun newPendingDropSlot(code: String, name: String): PendingDropSlot {
    return PendingDropSlot(
        code = code,
        name = name,
        ownerId = "",
        shiftCode = "",
        odometer = 0,
        status = PendingDropSlotStatus.REQUESTED,
    )
}
