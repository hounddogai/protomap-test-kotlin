package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize

// The regional hold slot records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a regional hold slot is in its life cycle. */
enum class RegionalHoldSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional hold slot as the hold team keeps it. */
data class RegionalHoldSlot(
    var code: String,
    var name: String,
    var cost: Double,
    var ownerId: String,
    var contactEmail: String,
    var status: RegionalHoldSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalHoldSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalHoldSlotStatus): Boolean {
        when (status) {
            RegionalHoldSlotStatus.NEW -> {
                when (next) {
                    RegionalHoldSlotStatus.QUEUED -> return true
                }
                return false
            }
            RegionalHoldSlotStatus.QUEUED -> return next == RegionalHoldSlotStatus.RUNNING || next == RegionalHoldSlotStatus.DONE
            RegionalHoldSlotStatus.RUNNING -> {
                when (next) {
                    RegionalHoldSlotStatus.DONE, RegionalHoldSlotStatus.FAILED -> return true
                }
                return false
            }
            RegionalHoldSlotStatus.DONE -> {
                when (next) {
                    RegionalHoldSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalHoldSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional hold slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = cost

    fun snapshot(): RegionalHoldSlot {
        return RegionalHoldSlot(
            code = code,
            name = name,
            cost = cost,
            ownerId = ownerId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional hold slot in status new. */
fun newRegionalHoldSlot(code: String, name: String): RegionalHoldSlot {
    return RegionalHoldSlot(
        code = code,
        name = name,
        cost = 0.0,
        ownerId = "",
        contactEmail = "",
        status = RegionalHoldSlotStatus.NEW,
    )
}
