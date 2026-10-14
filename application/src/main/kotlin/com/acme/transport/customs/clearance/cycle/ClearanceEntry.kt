package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.normalize

/** Where a clearance entry is in its life cycle. */
enum class ClearanceEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A clearance entry as the clearance team keeps it. */
data class ClearanceEntry(
    var code: String,
    var name: String,
    var lane: String,
    var active: Boolean,
    var checksum: String,
    var quantity: Int,
    var status: ClearanceEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceEntryStatus): Boolean {
        when (status) {
            ClearanceEntryStatus.PROPOSED -> {
                when (next) {
                    ClearanceEntryStatus.APPROVED, ClearanceEntryStatus.REJECTED -> return true
                }
                return false
            }
            ClearanceEntryStatus.APPROVED -> {
                when (next) {
                    ClearanceEntryStatus.REJECTED, ClearanceEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            ClearanceEntryStatus.REJECTED -> {
                when (next) {
                    ClearanceEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceEntryStatus) {
        if (!canMoveTo(next)) {
            error("clearance entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"clearance entry"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ClearanceEntry {
        return ClearanceEntry(
            code = code,
            name = name,
            lane = lane,
            active = active,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a clearance entry in status proposed. */
fun newClearanceEntry(code: String, name: String): ClearanceEntry {
    return ClearanceEntry(
        code = code,
        name = name,
        lane = "",
        active = false,
        checksum = "",
        quantity = 0,
        status = ClearanceEntryStatus.PROPOSED,
    )
}
