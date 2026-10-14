package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door slot is in its life cycle. */
enum class DockDoorSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dock door slot, with what the planners need to act on it. */
data class DockDoorSlot(
    var code: String,
    var name: String,
    var locale: String,
    var endsAt: Int,
    var carrier: String,
    var driverId: String,
    var trailerId: String,
    var label: String,
    var phone: String,
    var threshold: Double,
    var status: DockDoorSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorSlotStatus): Boolean {
        when (status) {
            DockDoorSlotStatus.PROPOSED -> return next == DockDoorSlotStatus.REJECTED || next == DockDoorSlotStatus.WITHDRAWN
            DockDoorSlotStatus.APPROVED -> return next == DockDoorSlotStatus.WITHDRAWN
            DockDoorSlotStatus.REJECTED -> {
                when (next) {
                    DockDoorSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorSlotStatus) {
        if (!canMoveTo(next)) {
            error("dock door slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun projectedLoad(): Double = round(endsAt.toDouble() * threshold * 100) / 100

    fun snapshot(): DockDoorSlot {
        return DockDoorSlot(
            code = code,
            name = name,
            locale = locale,
            endsAt = endsAt,
            carrier = carrier,
            driverId = driverId,
            trailerId = trailerId,
            label = label,
            phone = phone,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dock door slot in status proposed. */
fun newDockDoorSlot(code: String, name: String): DockDoorSlot {
    return DockDoorSlot(
        code = code,
        name = name,
        locale = "",
        endsAt = 0,
        carrier = "",
        driverId = "",
        trailerId = "",
        label = "",
        phone = "",
        threshold = 0.0,
        status = DockDoorSlotStatus.PROPOSED,
    )
}
