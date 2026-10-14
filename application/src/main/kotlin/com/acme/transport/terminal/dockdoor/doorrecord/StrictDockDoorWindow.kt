package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize

/** Where a strict dock door window is in its life cycle. */
enum class StrictDockDoorWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict dock door window, with what the planners need to act on it. */
data class StrictDockDoorWindow(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var fuelLiters: Double,
    var carrier: String,
    var summary: String,
    var status: StrictDockDoorWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorWindowStatus): Boolean {
        when (status) {
            StrictDockDoorWindowStatus.REQUESTED -> return next == StrictDockDoorWindowStatus.GRANTED || next == StrictDockDoorWindowStatus.REVOKED
            StrictDockDoorWindowStatus.GRANTED -> return next == StrictDockDoorWindowStatus.REVOKED || next == StrictDockDoorWindowStatus.EXPIRED
            StrictDockDoorWindowStatus.REVOKED -> {
                when (next) {
                    StrictDockDoorWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDockDoorWindowStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): StrictDockDoorWindow {
        return StrictDockDoorWindow(
            code = code,
            name = name,
            pinned = pinned,
            fuelLiters = fuelLiters,
            carrier = carrier,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict dock door window in status requested. */
fun newStrictDockDoorWindow(code: String, name: String): StrictDockDoorWindow {
    return StrictDockDoorWindow(
        code = code,
        name = name,
        pinned = false,
        fuelLiters = 0.0,
        carrier = "",
        summary = "",
        status = StrictDockDoorWindowStatus.REQUESTED,
    )
}
