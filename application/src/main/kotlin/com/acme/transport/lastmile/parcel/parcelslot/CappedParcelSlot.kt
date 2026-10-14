package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped parcel slot records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a capped parcel slot is in its life cycle. */
enum class CappedParcelSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped parcel slot as the parcel team keeps it. */
data class CappedParcelSlot(
    var code: String,
    var label: String,
    var odometer: Int,
    var pallets: Int,
    var limit: Int,
    var status: CappedParcelSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedParcelSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedParcelSlotStatus): Boolean {
        when (status) {
            CappedParcelSlotStatus.IDLE -> return next == CappedParcelSlotStatus.STOPPED
            CappedParcelSlotStatus.WARMING -> {
                when (next) {
                    CappedParcelSlotStatus.DRAINING -> return true
                }
                return false
            }
            CappedParcelSlotStatus.READY -> {
                when (next) {
                    CappedParcelSlotStatus.STOPPED -> return true
                }
                return false
            }
            CappedParcelSlotStatus.DRAINING -> return next == CappedParcelSlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedParcelSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped parcel slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round(odometer.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): CappedParcelSlot {
        return CappedParcelSlot(
            code = code,
            label = label,
            odometer = odometer,
            pallets = pallets,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped parcel slot in status idle. */
fun newCappedParcelSlot(code: String, label: String): CappedParcelSlot {
    return CappedParcelSlot(
        code = code,
        label = label,
        odometer = 0,
        pallets = 0,
        limit = 0,
        status = CappedParcelSlotStatus.IDLE,
    )
}
