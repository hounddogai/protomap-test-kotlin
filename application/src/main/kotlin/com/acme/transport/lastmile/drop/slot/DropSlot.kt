package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop slot records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop slot is in its life cycle. */
enum class DropSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a drop slot and its current status. */
data class DropSlot(
    var code: String,
    var title: String,
    var region: String,
    var loadKg: Double,
    var cost: Double,
    var driverId: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var sequence: Int,
    var status: DropSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropSlotStatus): Boolean {
        when (status) {
            DropSlotStatus.REQUESTED -> return next == DropSlotStatus.GRANTED
            DropSlotStatus.GRANTED -> return next == DropSlotStatus.REVOKED
            DropSlotStatus.REVOKED -> return next == DropSlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DropSlotStatus) {
        if (!canMoveTo(next)) {
            error("drop slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop slot"} $code"

    fun adjustedVolume(): Double = round((loadKg + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DropSlot {
        return DropSlot(
            code = code,
            title = title,
            region = region,
            loadKg = loadKg,
            cost = cost,
            driverId = driverId,
            visible = visible,
            etaMinutes = etaMinutes,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a drop slot in status requested. */
fun newDropSlot(code: String, title: String): DropSlot {
    return DropSlot(
        code = code,
        title = title,
        region = "",
        loadKg = 0.0,
        cost = 0.0,
        driverId = "",
        visible = false,
        etaMinutes = 0,
        sequence = 0,
        status = DropSlotStatus.REQUESTED,
    )
}
