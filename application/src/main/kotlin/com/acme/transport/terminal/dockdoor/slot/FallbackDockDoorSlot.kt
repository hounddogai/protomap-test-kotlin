package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.normalize

// The fallback dock door slot records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a fallback dock door slot is in its life cycle. */
enum class FallbackDockDoorSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback dock door slot as the dock door team keeps it. */
data class FallbackDockDoorSlot(
    var code: String,
    var label: String,
    var category: String,
    var locale: String,
    var version: Int,
    var status: FallbackDockDoorSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDockDoorSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDockDoorSlotStatus): Boolean {
        when (status) {
            FallbackDockDoorSlotStatus.IDLE -> {
                when (next) {
                    FallbackDockDoorSlotStatus.READY, FallbackDockDoorSlotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackDockDoorSlotStatus.WARMING -> return next == FallbackDockDoorSlotStatus.STOPPED
            FallbackDockDoorSlotStatus.READY -> {
                when (next) {
                    FallbackDockDoorSlotStatus.DRAINING, FallbackDockDoorSlotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackDockDoorSlotStatus.DRAINING -> {
                when (next) {
                    FallbackDockDoorSlotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDockDoorSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback dock door slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): FallbackDockDoorSlot {
        return FallbackDockDoorSlot(
            code = code,
            label = label,
            category = category,
            locale = locale,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback dock door slot in status idle. */
fun newFallbackDockDoorSlot(code: String, label: String): FallbackDockDoorSlot {
    return FallbackDockDoorSlot(
        code = code,
        label = label,
        category = "",
        locale = "",
        version = 0,
        status = FallbackDockDoorSlotStatus.IDLE,
    )
}
