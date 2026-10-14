package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.text.normalize

// The regional parcel slot records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a regional parcel slot is in its life cycle. */
enum class RegionalParcelSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional parcel slot and its current status. */
data class RegionalParcelSlot(
    var code: String,
    var title: String,
    var channel: String,
    var externalRef: String,
    var dock: String,
    var amount: Double,
    var status: RegionalParcelSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelSlotStatus): Boolean {
        when (status) {
            RegionalParcelSlotStatus.NEW -> {
                when (next) {
                    RegionalParcelSlotStatus.QUEUED, RegionalParcelSlotStatus.RUNNING -> return true
                }
                return false
            }
            RegionalParcelSlotStatus.QUEUED -> return next == RegionalParcelSlotStatus.DONE
            RegionalParcelSlotStatus.RUNNING -> return next == RegionalParcelSlotStatus.DONE || next == RegionalParcelSlotStatus.FAILED
            RegionalParcelSlotStatus.DONE -> return next == RegionalParcelSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalParcelSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = amount

    fun snapshot(): RegionalParcelSlot {
        return RegionalParcelSlot(
            code = code,
            title = title,
            channel = channel,
            externalRef = externalRef,
            dock = dock,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional parcel slot in status new. */
fun newRegionalParcelSlot(code: String, title: String): RegionalParcelSlot {
    return RegionalParcelSlot(
        code = code,
        title = title,
        channel = "",
        externalRef = "",
        dock = "",
        amount = 0.0,
        status = RegionalParcelSlotStatus.NEW,
    )
}
