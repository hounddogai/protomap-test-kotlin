package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize

// The tire entry records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire entry is in its life cycle. */
enum class TireEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tire entry as the tire team keeps it. */
data class TireEntry(
    var code: String,
    var label: String,
    var quantity: Int,
    var shiftCode: String,
    var reviewer: String,
    var status: TireEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireEntryStatus): Boolean {
        when (status) {
            TireEntryStatus.IDLE -> {
                when (next) {
                    TireEntryStatus.DRAINING -> return true
                }
                return false
            }
            TireEntryStatus.WARMING -> return next == TireEntryStatus.DRAINING
            TireEntryStatus.READY -> {
                when (next) {
                    TireEntryStatus.DRAINING -> return true
                }
                return false
            }
            TireEntryStatus.DRAINING -> return next == TireEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TireEntryStatus) {
        if (!canMoveTo(next)) {
            error("tire entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): TireEntry {
        return TireEntry(
            code = code,
            label = label,
            quantity = quantity,
            shiftCode = shiftCode,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire entry in status idle. */
fun newTireEntry(code: String, label: String): TireEntry {
    return TireEntry(
        code = code,
        label = label,
        quantity = 0,
        shiftCode = "",
        reviewer = "",
        status = TireEntryStatus.IDLE,
    )
}
