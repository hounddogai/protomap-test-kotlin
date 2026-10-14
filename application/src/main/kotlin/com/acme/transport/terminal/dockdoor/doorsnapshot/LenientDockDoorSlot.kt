package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dock door slot is in its life cycle. */
enum class LenientDockDoorSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient dock door slot as the dock door team keeps it. */
data class LenientDockDoorSlot(
    var code: String,
    var label: String,
    var version: Int,
    var weight: Double,
    var externalRef: String,
    var archived: Boolean,
    var segment: String,
    var loadKg: Double,
    var batchSize: Int,
    var cost: Double,
    var status: LenientDockDoorSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDockDoorSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDockDoorSlotStatus): Boolean {
        when (status) {
            LenientDockDoorSlotStatus.PROPOSED -> {
                when (next) {
                    LenientDockDoorSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientDockDoorSlotStatus.APPROVED -> return next == LenientDockDoorSlotStatus.REJECTED || next == LenientDockDoorSlotStatus.WITHDRAWN
            LenientDockDoorSlotStatus.REJECTED -> {
                when (next) {
                    LenientDockDoorSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDockDoorSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient dock door slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round(cost * batchSize.toDouble() * 100) / 100

    fun snapshot(): LenientDockDoorSlot {
        return LenientDockDoorSlot(
            code = code,
            label = label,
            version = version,
            weight = weight,
            externalRef = externalRef,
            archived = archived,
            segment = segment,
            loadKg = loadKg,
            batchSize = batchSize,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient dock door slot in status proposed. */
fun newLenientDockDoorSlot(code: String, label: String): LenientDockDoorSlot {
    return LenientDockDoorSlot(
        code = code,
        label = label,
        version = 0,
        weight = 0.0,
        externalRef = "",
        archived = false,
        segment = "",
        loadKg = 0.0,
        batchSize = 0,
        cost = 0.0,
        status = LenientDockDoorSlotStatus.PROPOSED,
    )
}
